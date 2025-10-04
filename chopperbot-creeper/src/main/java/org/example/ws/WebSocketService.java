package org.example.ws;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author wangmin
 * @version 1.0
 * @description TODO
 * @date 2025/9/21 15:15
 */
@Service
public class WebSocketService {
    private static final Map<String, Boolean> roomIdMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private StandardWebSocketClient webSocketClient;
    private DouyinWebSocketHandler webSocketHandler;
    private WebSocketConnectionManager connectionManager;

    @PostConstruct
    public void init() {
        webSocketHandler = new DouyinWebSocketHandler();
        // 初始化WebSocket客户端
        webSocketClient = new StandardWebSocketClient();
        webSocketClient.setSslContext(SSLConfig.getOrCreateSSLContext());

    }

    public void connectToDouyin(String roomId, String uri, WebSocketHttpHeaders headers) {
        connectionManager = new WebSocketConnectionManager(webSocketClient, webSocketHandler, URI.create(uri));
        connectionManager.setHeaders(headers);
        connectionManager.setOrigin("https://webcast100-ws-web-lf.douyin.com");
        try {
            if (!roomIdMap.getOrDefault(roomId, false)) {
                this.connectionManager.start();
                roomIdMap.put(roomId, true);
                System.out.println("🔄 WebSocket 客户端启动中...");
            } else {
                System.out.println("❌ WebSocket 链接已存在，请勿重复启动.");
            }
        } catch (Exception e) {
            roomIdMap.put(roomId, false);
            System.err.println("❌ 启动失败: " + e.getMessage());
            scheduleReconnect(roomId, uri, headers);
        }
//        webSocketClient.execute(douyinWebSocketClient, headers, URI.create(uri)).get();
    }

    private void scheduleReconnect(String roomId, String uri, WebSocketHttpHeaders headers) {
        System.out.println("⏳ 5秒后尝试重连...");
        scheduler.schedule(() -> {
            connectToDouyin(roomId, uri, headers);
        }, 5, TimeUnit.SECONDS);
    }

    // 可选：提供手动重连接口
    public void reconnect(String roomId, String uri, WebSocketHttpHeaders headers) {
        if (connectionManager != null) {
            connectionManager.stop();
        }
        connectToDouyin(roomId, uri, headers);
    }


}
