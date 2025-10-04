package org.example.ws;

/**
 * @author wangmin
 * @version 1.0
 * @description TODO
 * @date 2025/9/21 12:39
 */

import lombok.Getter;
import org.example.protocol.douyin.ChatMessage;
import org.example.protocol.douyin.GiftMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Getter
public class DouyinLiveWebFetcher {

    // 消息处理器映射
    private Map<String, MessageHandler> messageHandlers = new HashMap<>();

    public DouyinLiveWebFetcher() {
        // 初始化消息处理器映射
        messageHandlers.put("WebcastChatMessage", this::parseChatMsg);
        messageHandlers.put("WebcastGiftMessage", this::parseGiftMsg);
        messageHandlers.put("WebcastLikeMessage", this::parseLikeMsg);
        messageHandlers.put("WebcastMemberMessage", this::parseMemberMsg);
        messageHandlers.put("WebcastSocialMessage", this::parseSocialMsg);
        messageHandlers.put("WebcastRoomUserSeqMessage", this::parseRoomUserSeqMsg);
        messageHandlers.put("WebcastFansclubMessage", this::parseFansclubMsg);
        messageHandlers.put("WebcastControlMessage", this::parseControlMsg);
        messageHandlers.put("WebcastEmojiChatMessage", this::parseEmojiChatMsg);
        messageHandlers.put("WebcastRoomStatsMessage", this::parseRoomStatsMsg);
        messageHandlers.put("WebcastRoomMessage", this::parseRoomMsg);
        messageHandlers.put("WebcastRoomRankMessage", this::parseRankMsg);
        messageHandlers.put("WebcastRoomStreamAdaptationMessage", this::parseRoomStreamAdaptationMsg);
    }


    /**
     * GZIP 解压缩
     *
     * @param compressedData 压缩的数据
     * @return 解压后的数据
     */
    public static byte[] decompressGzip(byte[] compressedData) {
        // 输入验证
        if (compressedData == null || compressedData.length == 0) {
            System.out.println("警告: 输入数据为null或空");
            return compressedData;
        }

        System.out.println("开始解压GZIP数据，长度: " + compressedData.length + " 字节");

        // 验证GZIP头
        if (compressedData.length < 2) {
            System.out.println("警告: 数据太短，不是有效的GZIP格式");
            return compressedData;
        }

        if (compressedData[0] != (byte) 0x1f || compressedData[1] != (byte) 0x8b) {
            System.out.println("警告: 数据不是有效的GZIP格式 (头字节: " +
                    String.format("0x%02X 0x%02X", compressedData[0], compressedData[1]) + ")");
            return compressedData;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
             GZIPInputStream gis = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192]; // 增大缓冲区
            int len;
            int totalRead = 0;

            System.out.println("GZIP流创建成功，开始读取数据...");

            while ((len = gis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
                totalRead += len;
                System.out.println("读取到 " + len + " 字节，累计: " + totalRead + " 字节");
            }

            System.out.println("GZIP解压完成，总共读取: " + totalRead + " 字节");
            return baos.toByteArray();

        } catch (IOException e) {
            System.err.println("GZIP解压失败，输入数据长度: " + compressedData.length + " 字节");
            System.err.println("错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("GZIP 解压缩失败", e);
        }
    }

    // 各种消息解析方法需要单独实现，例如：
    private void parseChatMsg(byte[] payload) throws Exception {
        ChatMessage message = ChatMessage.parseFrom(payload);
        String userName = message.getUser().getNickName();
        long userId = message.getUser().getId();
        String content = message.getContent();
        System.out.println("【聊天msg】[" + userId + "]" + userName + ": " + content);
    }

    private void parseGiftMsg(byte[] payload) throws Exception {
        GiftMessage message = GiftMessage.parseFrom(payload);
        String userName = message.getUser().getNickName();
        String giftName = message.getGift().getName();
        long giftCnt = message.getComboCount();
        System.out.println("【礼物msg】" + userName + " 送出了 " + giftName + "x" + giftCnt);
    }

    // 其他消息处理方法...
    private void parseLikeMsg(byte[] payload) { /* 实现省略 */ }

    private void parseMemberMsg(byte[] payload) { /* 实现省略 */ }

    private void parseSocialMsg(byte[] payload) { /* 实现省略 */ }

    private void parseRoomUserSeqMsg(byte[] payload) { /* 实现省略 */ }

    private void parseFansclubMsg(byte[] payload) { /* 实现省略 */ }

    private void parseControlMsg(byte[] payload) { /* 实现省略 */ }

    private void parseEmojiChatMsg(byte[] payload) { /* 实现省略 */ }

    private void parseRoomStatsMsg(byte[] payload) { /* 实现省略 */ }

    private void parseRoomMsg(byte[] payload) { /* 实现省略 */ }

    private void parseRankMsg(byte[] payload) { /* 实现省略 */ }

    private void parseRoomStreamAdaptationMsg(byte[] payload) { /* 实现省略 */ }

    // 消息处理器接口
    @FunctionalInterface
    public interface MessageHandler {
        void handle(byte[] payload) throws Exception;
    }
}

