package org.example;

import org.apache.commons.lang.StringUtils;
import org.example.util.HttpClientUtil;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v139.network.Network;
import org.openqa.selenium.devtools.v139.network.model.Cookie;
import org.openqa.selenium.devtools.v139.network.model.Request;
import org.openqa.selenium.devtools.v139.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v139.network.model.ResponseReceived;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChromeDriverHeaderTest {

    @Test
    public void captureRequestHeaders() {
        // 设置ChromeDriver路径
        System.setProperty("webdriver.chrome.driver", "D:\\Program Files\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        options.addArguments("--remote-allow-origins=*");

        ChromeDriver driver = new ChromeDriver(options);

        // 获取DevTools实例
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        // 访问网页
        driver.get("https://live.douyin.com/284221393020");
        // 启用网络域
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        String[] strings = new String[2];
        Map<String, String> headers = new HashMap<>();
        // 添加网络请求监听器
//        devTools.addListener(Network.requestWillBeSent(), (RequestWillBeSent requestWillBeSent) -> {
//            Request request = requestWillBeSent.getRequest();
//            System.out.println("请求URL: " + request.getUrl());
//            System.out.println("请求方法: " + request.getMethod());
//            if (StringUtils.contains(request.getUrl(), "https://live.douyin" +
//                    ".com/webcast/room/web/enter/?aid")) {
//                System.out.println("请求头信息:");
//                StringBuilder sb = new StringBuilder();
//                request.getHeaders().toJson().forEach((name, value) -> {
//                    sb.append(name).append("=").append(value).append("; ");
//                });
//                headers.put("Cookie", sb.toString());
//                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
//                        "Chrome/140.0.0.0 Safari/537.36");
//            }
//        });

        devTools.addListener(Network.responseReceived(), (ResponseReceived responseReceived) -> {
            if (StringUtils.contains(responseReceived.getResponse().getUrl(), "https://live.douyin" +
                    ".com/webcast/room/web/enter/?aid")) {
                strings[0] = responseReceived.getResponse().getUrl();
            }
        });

        try {
            // 等待一段时间以捕获请求
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Cookie> cookies = devTools.send(Network.getCookies(Optional.empty()));
        String cookieString = cookies.stream()
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining("; "));
        headers.put("Cookie", cookieString);
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/140.0.0.0 Safari/537.36");
        driver.quit();

        System.out.println("请求结束");

        // 使用HttpClientUtil发送GET请求
        String response = HttpClientUtil.get(strings[0], headers);
        System.out.println(response);
    }
}