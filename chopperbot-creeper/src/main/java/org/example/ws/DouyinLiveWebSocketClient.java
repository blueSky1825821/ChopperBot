package org.example.ws;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.constpool.ConstCreeper;
import org.example.util.ByteDanceUtil;
import org.example.utils.DouyinUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHttpHeaders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * @author wangmin
 * @version 1.0
 * @description TODO
 * @date 2025/9/20 22:26
 */
@Slf4j
@Service
public class DouyinLiveWebSocketClient {
    @Autowired
    private WebSocketService webSocketService;

    /**
     * 断开WebSocket连接
     */
    public static void disconnect(Session session) {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            log.error("关闭WebSocket连接时出错: " + e.getMessage());
        }
    }


    /**
     * 连接抖音直播间WebSocket服务器
     */
    public void connectWebSocket(String roomNo) throws Exception {
        // 构建WebSocket URL
        String appName = "douyin_web";
        String versionCode = "180800";
        String webcastSdkVersion = "1.0.14-beta.0";
        String updateVersionCode = "1.0.14-beta.0";
        String devicePlatform = "web";
        String browserLanguage = "zh-CN";
        String browserPlatform = "Win32";
        String browserName = "Mozilla";
        String browserVersion = "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126" +
                ".0.0.0 Safari/537.36";
        String tzName = "Asia/Shanghai";
        String host = "https://live.douyin.com";
        String aid = "6383";
        String liveId = "1";
        String didRule = "3";
        String endpoint = "live_pc";
        String identity = "audience";
        String imPath = "/webcast/im/fetch/";

        // 正确编码浏览器版本字符串
        String encodedBrowserVersion = URLEncoder.encode(browserVersion, StandardCharsets.UTF_8);
        String encodedHost = URLEncoder.encode(host, StandardCharsets.UTF_8);
        String encodedTzName = URLEncoder.encode(tzName, StandardCharsets.UTF_8);
        String roomId = DouyinUtils.getRoomId(roomNo);

        // 构建内部参数并进行编码
        String internalExt = "internal_src:dim|wss_push_room_id:" + roomId + "|wss_push_did:7319483754668557238" +
                "|first_req_ms:1721106114541|fetch_time:1721106114633|seq:1|wss_info:0-1721106114633-0-0|" +
                "wrds_v:7392094459690748497";
        String encodedInternalExt = URLEncoder.encode(internalExt, StandardCharsets.UTF_8);

        // 构建WebSocket URL（使用编码后的参数）
        String wss = "wss://webcast100-ws-web-lf.douyin.com/webcast/im/push/v2/?app_name=douyin_web&version_code" +
                "=180800&webcast_sdk_version=1.0.14-beta.0&update_version_code=1.0.14-beta" +
                ".0&compress=gzip&device_platform=web&cookie_enabled=true&screen_width=1536&screen_height=864" +
                "&browser_language=zh-CN&browser_platform=Win32&browser_name=Mozilla&browser_version=5.0%20" +
                "(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/126.0" +
                ".0.0%20Safari/537.36&browser_online=true&tz_name=Asia/Shanghai&cursor=d-1_u-1_fh" +
                "-7392091211001140287_t-1721106114633_r-1&internal_ext=" +
                encodedInternalExt + "&host=https://live" +
                ".douyin.com&aid=6383&live_id=1&did_rule=3&endpoint=live_pc&support_wrds=1&user_unique_id" +
                "=7319483754668557238&im_path=/webcast/im/fetch/&identity=audience&need_persist_msg_count=15" +
                "&insert_task_id=&live_reason=&room_id=" + roomId + "&heartbeatDuration=0&signature" +
                "=fBBxlyFCBPWvUCiV";

        // 生成签名
        String signature = DouyinUtils.generateSignature(wss, "sign.js");
        wss = wss + "&signature=" + signature;

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.put("cookie", Collections.singletonList("ttwid=" + ByteDanceUtil.getTtwid()));
        headers.put("user-agent", Collections.singletonList(ConstCreeper.USER_AGENT));
        webSocketService.connectToDouyin(roomId, wss, headers);
        // 创建WebSocket客户端并连接
//        DouyinWebSocketClient client = new DouyinWebSocketClient();
//        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
//            @Override
//            public void beforeRequest(Map<String, List<String>> headers) {
//                headers.put("cookie", Collections.singletonList("ttwid=" + ByteDanceUtil.getTtwid()));
//                headers.put("user-agent", Collections.singletonList(ConstCreeper.USER_AGENT));
//            }
//        };
//
//        ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
//                .configurator(configurator)
//                .build();
//        Session session = null;
//
//        try {
//            URI uri = new URI(wss);
//            WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
//            session = webSocketContainer.connectToServer(client, clientConfig, uri);
//
//            // 保持连接活跃
//            while (session.isOpen()) {
//                Thread.sleep(1000);
//            }
//        } catch (URISyntaxException e) {
//            log.error("WebSocket URL语法错误: " + e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("WebSocket连接异常: " + e.getMessage());
//            if (Objects.nonNull(session)) {
//                disconnect(session);
//            }
//            throw e;
//        }
    }
}
