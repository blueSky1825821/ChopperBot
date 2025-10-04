package org.example.ws;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.utils.DouyinUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHttpHeaders;

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
        String roomId = DouyinUtils.getRoomId(roomNo);
        String wss = DouyinUtils.wss(roomId);
        WebSocketHttpHeaders headers = DouyinUtils.headers();
        webSocketService.connectToDouyin(roomId, wss, headers);

    }
}
