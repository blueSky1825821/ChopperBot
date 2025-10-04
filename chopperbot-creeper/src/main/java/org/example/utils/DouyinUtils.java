package org.example.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.example.constpool.ConstCreeper;
import org.example.util.ByteDanceUtil;
import org.example.util.HttpClientUtil;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.springframework.web.socket.WebSocketHttpHeaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.example.util.ByteDanceUtil.generateMsToken;
import static org.example.util.ByteDanceUtil.getTtwid;

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
            String[] params = { "live_id", "aid", "version_code", "webcast_sdk_version",
                    "room_id", "sub_room_id", "sub_channel_id", "did_rule",
                    "user_unique_id", "device_platform", "device_type", "ac",
                    "identity"};
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
    @SneakyThrows
    public static Map<String, String> parseWssParams(String wssUrl) {
        String query = wssUrl.split("\\?")[1];
        Map<String, String> wssParamsMap = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    wssParamsMap.put(keyValue[0], keyValue[1]);
                } else {
                    wssParamsMap.put(keyValue[0], "");
                }
            }
        }

        return wssParamsMap;
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

    /**
     * 根据直播间的地址获取到真正的直播间roomId，有时会有错误，可以重试请求解决
     *
     * @return room_id
     */
    public static String getRoomId(String roomNo) {
        String url = liveUrl + roomNo;
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", ConstCreeper.USER_AGENT);
        headers.put("Cookie", "ttwid=" + getTtwid() + "&msToken=" + generateMsToken() + "; __ac_nonce" +
                "=0123407cc00a9e438deb4");
        String responseBody = HttpClientUtil.get(url, headers);
        if (StringUtils.isNotBlank(responseBody)) {
            Pattern pattern = Pattern.compile("roomId\\\\\":\\\\\"(\\d+)\\\\\"");
            Matcher matcher = pattern.matcher(responseBody);

            if (matcher.find()) {
                return matcher.group(1);
            } else {
                System.out.println("【X】No match found for roomId");
                return null;
            }
        }
        return null;
    }

    public static String wss(String roomId) {
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
                "&insert_task_id=&live_reason=&room_id=" + roomId + "&heartbeatDuration=0";

        // 生成签名
        String signature = DouyinUtils.generateSignature(wss, "sign.js");
        wss = wss + "&signature=" + signature;
        return wss;
    }

    public static WebSocketHttpHeaders headers()  {
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.put("cookie", List.of("ttwid=" + ByteDanceUtil.getTtwid()));
        headers.put("user-agent", List.of(ConstCreeper.USER_AGENT));
        return headers;
    }

    public static Map<String, List<String>> mapListHeaders()  {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("cookie", List.of("ttwid=" + ByteDanceUtil.getTtwid()));
        headers.put("user-agent", List.of(ConstCreeper.USER_AGENT));
        headers.put("Origin", List.of("https://webcast100-ws-web-lf.douyin.com"));
        return headers;
    }

    public static Map<String, String> mapHeaders()  {
        Map<String, String> headers = new HashMap<>();
        headers.put("cookie","ttwid=" + ByteDanceUtil.getTtwid());
        headers.put("user-agent", ConstCreeper.USER_AGENT);
        headers.put("Origin", "https://webcast100-ws-web-lf.douyin.com");
        return headers;
    }

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

        System.out.println(getRoomId("730765733998"));
    }
}
