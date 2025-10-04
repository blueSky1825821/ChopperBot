package org.example.ws;

/**
 * @author wangmin
 * @version 1.0
 * @description WebSocket客户端实现
 * @date 2025/9/20 22:21
 */

import com.google.protobuf.ByteString;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.protocol.douyin.Message;
import org.example.protocol.douyin.PushFrame;
import org.example.protocol.douyin.Response;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket客户端实现
 */
@ClientEndpoint
@Slf4j
public class DouyinWebSocketClient extends Endpoint {
    private ScheduledExecutorService scheduler;

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        log.info("【√】WebSocket连接成功.");
        // 启动心跳线程
        startHeartbeat(session);
    }

    @OnMessage
    public void onMessage(ByteBuffer buffer, Session session) {
        byte[] message = new byte[buffer.remaining()];        // 处理接收到的二进制消息
        log.info("收到二进制消息，长度: " + message.length);
        try {
            // 根据proto结构体解析对象
            PushFrame msg = PushFrame.parseFrom(message);
            DouyinLiveWebFetcher fetcher = new DouyinLiveWebFetcher();

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
                session.getBasicRemote().sendBinary(java.nio.ByteBuffer.wrap(ack.toByteArray()));
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

//    @OnMessage
//    public void onMessage(String message, Session session) {
//        // 处理接收到的消息
//        log.info("收到消息: " + message);
//        // 这里需要根据实际协议解析消息
//        try {
//            // 根据proto结构体解析对象
//            PushFrame msg = PushFrame.parseFrom(message.getBytes());
//            DouyinLiveWebFetcher fetcher = new DouyinLiveWebFetcher();
//
//            // 解压缩payload
//            byte[] decompressedPayload = fetcher.gzipDecompress(msg.getPayload().toByteArray());
//            Response response = Response.parseFrom(decompressedPayload);
//
//            // 返回直播间服务器链接存活确认消息，便于持续获取数据
//            if (response.getNeedAck()) {
//                PushFrame ack = PushFrame.newBuilder()
//                        .setLogId(msg.getLogId())
//                        .setPayloadType("ack")
//                        .setPayload(ByteString.copyFromUtf8(response.getInternalExt()))
//                        .build();
//                session.getBasicRemote().sendObject(ack);
//            }
//            Map<String, DouyinLiveWebFetcher.MessageHandler> messageHandlers = fetcher.getMessageHandlers();
//            // 根据消息类别解析消息体
//            for (Message mess : response.getMessagesListList()) {
//                String method = mess.getMethod();
//                DouyinLiveWebFetcher.MessageHandler handler = messageHandlers.get(method);
//                if (handler != null) {
//                    try {
//                        handler.handle(msg.getPayload().toByteArray());
//                    } catch (Exception e) {
//                        // 忽略处理异常
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("处理WebSocket消息时出错: " + e.getMessage());
//
//        }
//    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket错误: " + throwable.getMessage());
    }


    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info("WebSocket连接已关闭: " + closeReason);
        // 关闭当前实例的定时任务
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 发送心跳包
     */
    private void startHeartbeat(Session session) {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (session.isOpen()) {
                try {
                    // 发送心跳包（需要根据实际协议构造）
                    PushFrame heartbeat = PushFrame.newBuilder().setPayloadType("hb").build();
                    session.getBasicRemote().sendBinary(ByteBuffer.wrap(heartbeat.toByteArray()));
                    log.info("【√】发送心跳包");
                } catch (Exception e) {
                    log.error("【X】心跳包检测错误: " + e.getMessage());
                }
            }
            // 每5秒发送一次心跳
        }, 0, 5, TimeUnit.SECONDS);
    }
}
