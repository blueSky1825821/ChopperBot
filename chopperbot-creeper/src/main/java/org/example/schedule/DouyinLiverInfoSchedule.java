package org.example.schedule;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.example.bean.FocusLiver;
import org.example.constpool.ConstCreeper;
import org.example.constpool.ConstPool;
import org.example.log.ChopperLogFactory;
import org.example.log.LoggerType;
import org.example.service.FocusLiverService;
import org.example.ws.DouyinLiveWebSocketClient;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v139.network.Network;
import org.openqa.selenium.devtools.v139.network.model.Cookie;
import org.openqa.selenium.devtools.v139.network.model.ResponseReceived;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wangmin
 * @version 1.0
 * @description 定时爬取抖音参数信息
 * @date 2025/9/16 23:41
 */
@Component
public class DouyinLiverInfoSchedule {
    private static final Logger LOGGER = ChopperLogFactory.getLogger(LoggerType.Creeper);
    // 提取 room_id_str 的正则表达式
    private static final Pattern ROOM_ID_STR_PATTERN = Pattern.compile("[?&]room_id_str=([^&]*)");
    @Autowired
    public FocusLiverService focusLiverService;
    @Autowired
    private DouyinLiveWebSocketClient client;

    @Scheduled(initialDelay = 5 * 1000, fixedRate = 10 * 60 * 1000)
    public void liverChecker() {
        try {
            client.connectWebSocket("296728101980");

            LOGGER.info("开始执行抖音参数信息定时任务");
            process();
            LOGGER.info("执行抖音参数信息定时任务结束");
        } catch (Exception e) {
            LOGGER.error("抖音参数信息定时任务执行异常", e);
        }
    }

    private void process() {
        List<FocusLiver> douyinFocusLivers = focusLiverService.getFocusLivers(ConstPool.DOUYIN);
        if (CollectionUtils.isEmpty(douyinFocusLivers)) {
            return;
        }
        // 设置ChromeDriver路径
        System.setProperty(ConstCreeper.WEBDRIVER_CHROME_DRIVER, ConstCreeper.WEBDRIVER_CHROME_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.setBinary(ConstCreeper.WEBDRIVER_CHROME_APP);
        options.addArguments(ConstCreeper.REMOTE_ALLOW_ORIGINS);
        Date ago = Date.from(LocalDateTime.now().minusHours(1L).atZone(ZoneId.systemDefault()).toInstant());

        for (FocusLiver douyinFocusLiver : douyinFocusLivers) {
            boolean after =
                    douyinFocusLiver.getUpdateTime().after(ago);
            if (after) {
                continue;
            }
            // 重用ChromeDriver实例，提高性能
            ChromeDriver driver = new ChromeDriver(options);
            JSONObject extJson = new JSONObject();
            try {
                // 确保只操作一个窗口
                String originalWindow = driver.getWindowHandle();
                // 访问网页
                driver.get("https://live.douyin.com/" + douyinFocusLiver.getRoomId());

                // 如果意外打开了多个标签页，关闭多余的
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalWindow.equals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        driver.close();
                    }
                }
                // 切换回原始窗口
                driver.switchTo().window(originalWindow);

                // 获取DevTools实例
                DevTools devTools = driver.getDevTools();
                devTools.createSession();
                //返回响应头 0 url 1 room_id_str
                final String[] strings = new String[2];
                // 启用网络域
                devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(),
                                Optional.empty()),
                        Duration.ofSeconds(5 * 60L));
                CountDownLatch count = new CountDownLatch(1);
                devTools.addListener(Network.responseReceived(), (ResponseReceived responseReceived) -> {
                    try {
                        if (StringUtils.contains(responseReceived.getResponse().getUrl(), "https://live.douyin" +
                                ".com/webcast/room/web/enter/?aid")) {
                            strings[0] = responseReceived.getResponse().getUrl();
                            extJson.put("enterUrl", strings[0]);
                            // 提取 room_id_str
                            strings[1] = extractRoomIdStr(strings[0]);
                            count.countDown();
                        }
                    } catch (Exception e) {
                        LOGGER.error("获取直播信息出错, url:{}", responseReceived.getResponse().getUrl());
                    }
                });
                //返回cookie
                List<Cookie> cookies = devTools.send(Network.getCookies(Optional.empty()),
                        Duration.ofSeconds(5 * 60L));
                if (CollectionUtils.isNotEmpty(cookies)) {
                    String cookieString = cookies.stream()
                            .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                            .collect(Collectors.joining("; "));
                    extJson.put("Cookie", cookieString);
                    extJson.put("User-Agent", ConstCreeper.USER_AGENT);
                }
                boolean await = count.await(5L * 60, TimeUnit.SECONDS);
                if (await) {
                    FocusLiver update = new FocusLiver();
                    update.setId(douyinFocusLiver.getId());
                    update.setUpdateTime(new Date());
                    update.setExt(extJson.toJSONString());
                    update.setRoomIdStr(strings[1]);
                    focusLiverService.updateLivers(update);
                    LOGGER.info("更新抖音主播信息成功，roomId: {}", douyinFocusLiver.getRoomId());
                } else {
                    LOGGER.error("处理抖音主播信息失败，roomId: {}", douyinFocusLiver.getRoomId());
                }
            } catch (InterruptedException e) {
                LOGGER.error("处理抖音主播信息失败，roomId: {}", douyinFocusLiver.getRoomId(), e);
                // 保留中断状态
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOGGER.error("处理抖音主播信息失败，roomId: {}", douyinFocusLiver.getRoomId(), e);
            } finally {
                // 确保资源被正确释放
                try {
                    driver.quit();
                } catch (Exception e) {
                    LOGGER.error("关闭ChromeDriver失败", e);
                }
            }
        }
    }

    public String extractRoomIdStr(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        Matcher matcher = ROOM_ID_STR_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


}