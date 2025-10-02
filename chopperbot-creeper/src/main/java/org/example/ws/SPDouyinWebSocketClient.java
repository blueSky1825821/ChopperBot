package org.example.ws;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.example.protocol.douyin.Message;
import org.example.protocol.douyin.PushFrame;
import org.example.protocol.douyin.Response;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author wangmin
 * @version 1.0
 * @description TODO
 * @date 2025/9/21 15:13
 */
@Slf4j
public class SPDouyinWebSocketClient extends AbstractWebSocketHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private DouyinLiveWebFetcher fetcher;

    public SPDouyinWebSocketClient() {
        this.fetcher = new DouyinLiveWebFetcher();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("【√】WebSocket连接成功.");
        // 启动心跳线程
        startHeartbeat(session);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        byte[] payload = message.getPayload().array();
        // 处理接收到的二进制消息
        log.info("收到二进制消息，长度: " + payload.length);
        processMessage(session, payload);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理接收到的文本消息（如果需要）
        log.info("收到文本消息: " + message.getPayload());
        // 如果文本消息也需要处理，可以在这里添加逻辑
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: " + exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket连接已关闭: " + status);
        // 关闭当前实例的定时任务
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 处理消息内容
     */
    private void processMessage(WebSocketSession session, byte[] message) {
        try {
            // 根据proto结构体解析对象
            PushFrame msg = PushFrame.parseFrom(message);
            if (msg.getPayload().toByteArray().length == 0) {
                return;
            }
            // 解压缩payload
            byte[] decompressedPayload = fetcher.decompressGzip(msg.getPayload().toByteArray());
            Response response = Response.parseFrom(decompressedPayload);

            // 返回直播间服务器链接存活确认消息，便于持续获取数据
            if (response.getNeedAck()) {
                PushFrame ack = PushFrame.newBuilder()
                        .setLogId(msg.getLogId())
                        .setPayloadType("ack")
                        .setPayload(ByteString.copyFromUtf8(response.getInternalExt()))
                        .build();
                session.sendMessage(new BinaryMessage(ack.toByteArray()));
            }

            Map<String, DouyinLiveWebFetcher.MessageHandler> messageHandlers = fetcher.getMessageHandlers();
            // 根据消息类别解析消息体
            for (Message mess : response.getMessagesListList()) {
                String method = mess.getMethod();
                DouyinLiveWebFetcher.MessageHandler handler = messageHandlers.get(method);
                if (handler != null) {
                    try {
                        handler.handle(mess.getPayload().toByteArray());
                    } catch (Exception e) {
                        // 忽略处理异常
                        log.error("处理消息失败: " + method, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理WebSocket二进制消息时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 发送心跳包
     */
    private void startHeartbeat(WebSocketSession session) {
        scheduler.scheduleAtFixedRate(() -> {
            if (session.isOpen()) {
                try {
                    // 发送心跳包（需要根据实际协议构造）
                    PushFrame heartbeat = PushFrame.newBuilder().setPayloadType("hb").build();
                    byte[] heartbeatBytes = heartbeat.toByteArray();
                    log.info("【√】发送心跳包，长度: {} 字节", heartbeatBytes.length);
                    log.info("【√】发送心跳包内容: {}", bytesToHex(heartbeatBytes));
                    session.sendMessage(new PingMessage(ByteBuffer.wrap(heartbeatBytes)));
                    log.info("【√】发送心跳包");
                } catch (Exception e) {
                    log.error("【X】心跳包检测错误: " + e.getMessage());
                }
            }
            // 每5秒发送一次心跳
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 将字节数组转换为十六进制字符串（类似抓包显示格式）
     */
    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            // 每16个字节换行，便于阅读
            if (i > 0 && i % 16 == 0) {
                sb.append("\n");
            }
            // 格式化为两位十六进制数
            sb.append(String.format("%02X ", bytes[i] & 0xFF));
        }
        return sb.toString().trim();
    }
}
