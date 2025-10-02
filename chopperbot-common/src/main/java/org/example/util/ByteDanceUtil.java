package org.example.util;

import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author dhx
 * @date 2024/5/26 13:16
 */
public class ByteDanceUtil {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36";

    private static final Random RANDOM = new Random(); // 复用 Random 实例

    public static String getTtwid() {
        Map<String, String> douyinHeader = new HashMap<>();
        douyinHeader.put("User-Agent", ByteDanceUtil.USER_AGENT);

        Map<String, String> respHeaders = HttpClientUtil.getResponseHeaders("https://live.douyin.com/",
                HttpMethod.GET, null, douyinHeader);
        String ttwid = RegexUtil.match(respHeaders.get("Set-Cookie"), "ttwid=([^;]+)")[1];
        if (ttwid != null) {
            return ttwid;
        }
        String ac_nonce = RegexUtil.match(respHeaders.get("Set-Cookie"), "__ac_nonce=([a-zA-Z0-9]+)")[1];
        douyinHeader.put("Cookie", String.format("__ac_nonce=%s", ac_nonce));
        respHeaders = HttpClientUtil.getResponseHeaders("https://live.douyin.com/", HttpMethod.GET, "", douyinHeader);
        return RegexUtil.match(respHeaders.get("Set-Cookie"), "ttwid=([^;]+)")[0];
    }

    /**
     * 产生请求头部cookie中的msToken字段，其实为随机的182位字符
     *
     * @param length 字符位数，默认182
     * @return msToken
     */
    public static String generateMsToken(int length) {
        StringBuilder randomStr = new StringBuilder();
        String baseStr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
        int len = baseStr.length() - 1;
        for (int i = 0; i < length; i++) {
            randomStr.append(baseStr.charAt(RANDOM.nextInt(len + 1)));
        }

        return randomStr.toString();
    }

    /**
     * 产生请求头部cookie中的msToken字段，使用默认长度182
     *
     * @return msToken
     */
    public static String generateMsToken() {
        return generateMsToken(182);
    }
}
