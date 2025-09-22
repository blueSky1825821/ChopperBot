package org.example.ws;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
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
    private WebSocketClient webSocketClient;
    private SPDouyinWebSocketClient douyinWebSocketClient;
    private WebSocketConnectionManager connectionManager;

    @PostConstruct
    public void init() {
        douyinWebSocketClient = new SPDouyinWebSocketClient();
        // 初始化WebSocket客户端
        webSocketClient = createWebSocketClientWithCustomSSL();
        // 设置系统代理用于抓包（确保本地有代理服务器运行在8888端口）
//        System.setProperty("https.proxyHost", "127.0.0.1");
//        System.setProperty("https.proxyPort", "8888");

    }

    public void connectToDouyin(String roomId, String uri, WebSocketHttpHeaders headers) {
        connectionManager = new WebSocketConnectionManager(webSocketClient, douyinWebSocketClient, uri);
        connectionManager.setHeaders(headers);
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

    /**
     * 创建支持自定义SSL的WebSocket客户端
     */
    private WebSocketClient createWebSocketClientWithCustomSSL() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            // 设置SSL上下文
            client.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", getOrCreateSSLContext());
            return client;
        } catch (Exception e) {
            throw new RuntimeException("创建WebSocket客户端失败", e);
        }
    }

    // 缓存SSLContext实例以提高性能
    private static volatile SSLContext cachedSSLContext = null;
    private static final Object sslContextLock = new Object();

    /**
     * 获取或创建SSL上下文（带缓存）
     */
    private SSLContext getOrCreateSSLContext() throws Exception {
        if (cachedSSLContext == null) {
            synchronized (sslContextLock) {
                if (cachedSSLContext == null) {
                    cachedSSLContext = createTrustAllSSLContext();
                }
            }
        }
        return cachedSSLContext;
    }

    /**
     * 创建信任所有证书的SSL上下文
     */
    private SSLContext createTrustAllSSLContext() throws Exception {
        // 使用更安全的TLS协议版本
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // 可以添加客户端证书验证逻辑
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // 可以添加服务器证书验证逻辑
                        // 例如：验证证书颁发者、有效期等
                    }
                }
        };
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }
}
