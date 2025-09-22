package org.example.utils;

import org.example.constpool.ConstCreeper;
import org.example.util.HttpClientUtil;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.springframework.http.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wangmin
 * @version 1.0
 * @description TODO
 * @date 2025/9/20 15:07
 */
public class DouyinUtils {
    private static final String liveUrl = "https://live.douyin.com/";
    public static String generateSignature(String wss, String scriptFile) {
        try {
            // ... 你的参数解析逻辑不变 ...
            String[] params = { /* ... */};
            Map<String, String> wssMaps = parseWssParams(wss);
            String md5Param = calculateMD5(buildParamString(params, wssMaps));

            // 读取 JS 文件
            String script = loadScript(scriptFile);

            // ✅ 创建 Context —— 现在确保已安装 'js'
            try (Context context = Context.newBuilder("js")
                    .option("engine.WarnInterpreterOnly", "false") // 可选：关闭警告
                    .allowIO(false)
                    .build()) {

                context.eval("js", script);

                Value getSignFunc = context.getBindings("js").getMember("get_sign");
                if (getSignFunc == null) {
                    throw new RuntimeException("JS function 'get_sign' not found. Check your JS file.");
                }

                Value result = getSignFunc.execute(md5Param);
                return result.asString();

            } catch (PolyglotException e) {
                throw new RuntimeException("GraalVM JS engine error: " + e.getMessage(), e);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ... 工具方法提取（保持代码整洁）...
    private static Map<String, String> parseWssParams(String wss) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] parts = wss.split("\\?", 2);
        if (parts.length < 2) return map;
        for (String param : parts[1].split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static String buildParamString(String[] keys, Map<String, String> values) {
        return Arrays.stream(keys)
                .map(key -> key + "=" + values.getOrDefault(key, ""))
                .collect(Collectors.joining(","));
    }

    private static String calculateMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String loadScript(String scriptFile) throws IOException {
        try (InputStream is = DouyinUtils.class.getClassLoader().getResourceAsStream(scriptFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    // 重载方法，使用默认的scriptFile参数
    public static String generateSignature(String wss) {
        return generateSignature(wss, "sign.js");
    }

    /**
     * 获取请求头部cookie中的ttwid字段
     * 访问抖音网页版直播间首页可以获取到响应cookie中的ttwid
     * @return ttwid
     */
//    public String getTtwid() {
//        // 如果已经获取过ttwid，直接返回
//        try {
//            Map<String, String> headers = new HashMap<>();
//            headers.put("User-Agent", ConstCreeper.USER_AGENT);
//
//            Map<String, String> responseHeaders = HttpClientUtil.getResponseHeaders(liveUrl, HttpMethod.GET, null, headers);
//            // 检查响应状态
//            if (response.statusCode() >= 200 && response.statusCode() < 300) {
//                // 从响应头中提取Set-Cookie字段
//                Map<String, List<String>> headers = response.headers().map();
//                List<String> setCookieHeaders = headers.get("set-cookie");
//
//                if (setCookieHeaders != null) {
//                    // 遍历所有Set-Cookie头寻找ttwid
//                    for (String cookieHeader : setCookieHeaders) {
//                        if (cookieHeader != null && cookieHeader.contains("ttwid=")) {
//                            // 解析ttwid值
//                            String[] parts = cookieHeader.split(";");
//                            for (String part : parts) {
//                                part = part.trim();
//                                if (part.startsWith("ttwid=")) {
//                                    this.ttwid = part.substring(6); // 去掉"ttwid="前缀
//                                    return this.ttwid;
//                                }
//                            }
//                        }
//                    }
//                }
//
//                System.err.println("【X】No ttwid found in response cookies");
//                return null;
//            } else {
//                System.err.println("【X】Request failed with status code: " + response.statusCode());
//                return null;
//            }
//
//        } catch (Exception err) {
//            System.err.println("【X】Request the live url error: " + err.getMessage());
//            return null;
//        }
//    }

    public static void main(String[] args) {
        String wss = "wss://webcast100-ws-web-lq.douyin.com/webcast/im/push/v2/?app_name=douyin_web&version_code" +
                "=180800&webcast_sdk_version=1.0.14-beta.0&update_version_code=1.0.14-beta" +
                ".0&compress=gzip&device_platform=web&cookie_enabled=true&screen_width=1536&screen_height=864" +
                "&browser_language=zh-CN&browser_platform=Win32&browser_name=Mozilla&browser_version=5.0%20" +
                "(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/126.0" +
                ".0.0%20Safari/537.36&browser_online=true&tz_name=Asia/Shanghai&cursor=d-1_u-1_fh" +
                "-7392091211001140287_t-1721106114633_r-1&internal_ext=internal_src:dim|wss_push_room_id:284221393020" +
                "|wss_push_did:7319483754668557238|first_req_ms:1721106114541|fetch_time:1721106114633|seq:1|wss_info" +
                ":0-1721106114633-0-0|wrds_v:7392094459690748497&host=https://live.douyin" +
                ".com&aid=6383&live_id=1&did_rule=3&endpoint=live_pc&support_wrds=1&user_unique_id" +
                "=7319483754668557238&im_path=/webcast/im/fetch/&identity=audience&need_persist_msg_count=15" +
                "&insert_task_id=&live_reason=&room_id=284221393020&heartbeatDuration=0";
        String s = generateSignature(wss);
        System.out.println(s);
    }
}
