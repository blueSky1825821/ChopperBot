package org.example.core.creeper.loadconfig;

import org.example.core.creeper.loadconfig.DouyinLiverCheckerConfig;
import org.example.util.ByteDanceUtil;
import org.example.util.HttpClientUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 抖音直播检测配置类的单元测试
 * 使用HttpClientUtil直接发送HTTP请求测试抖音直播接口
 */
public class DouyinLiverCheckerConfigTest {

    private String testRoomId;

    @BeforeEach
    public void setUp() {
        // 使用示例房间ID
        testRoomId = "349213906905";
    }

    @Test
    public void test() {
        String ttwid = ByteDanceUtil.getTtwid();
        System.out.println(ttwid);
    }

    /**
     * 测试使用HttpClientUtil发送抖音直播请求
     * 注意：此测试需要网络连接，并且可能因为抖音的反爬虫机制而失败
     */
    @Test
    @Disabled("由于抖音反爬虫机制以及需要有效的Cookie，此测试可能无法正常运行")
    public void testDouyinLiveRequestWithHttpClientUtil() {
        try {
            // 创建配置对象
            DouyinLiverCheckerConfig config = new DouyinLiverCheckerConfig(testRoomId);
            
            // 获取配置中的URL和请求头
            String url = config.getUrl();
            Map<String, String> headers = config.getHeader();
            
            System.out.println("请求URL: " + url);
            System.out.println("请求头: " + headers);
            
            // 使用HttpClientUtil发送GET请求
            String response = HttpClientUtil.get(url, headers);
            
            // 验证响应不为空
            assertNotNull(response, "响应内容不应为空");
            assertTrue(response.length() > 0, "响应内容应该包含数据");
            
            System.out.println("响应长度: " + response.length());
            System.out.println("响应预览: " + (response.length() > 200 ? response.substring(0, 200) : response));
            
        } catch (Exception e) {
            fail("请求抖音直播接口时发生异常: " + e.getMessage(), e);
        }
    }

    /**
     * 测试配置对象的基本功能
     */
    @Test
    public void testDouyinLiverCheckerConfigCreation() {
        // 创建配置对象
        DouyinLiverCheckerConfig config = new DouyinLiverCheckerConfig(testRoomId);
        
        // 验证配置对象创建成功
        assertNotNull(config, "配置对象不应为空");
        assertNotNull(config.getUrl(), "URL不应为空");
        assertNotNull(config.getHeader(), "请求头不应为空");
        assertNotNull(config.getTaskId(), "任务ID不应为空");
        
        // 验证URL包含正确的参数
        assertTrue(config.getUrl().contains("webcast/room/web/enter"), "URL应包含直播接口路径");
        assertTrue(config.getUrl().contains(testRoomId), "URL应包含房间ID");
        
        // 验证请求头包含必要字段
        Map<String, String> headers = config.getHeader();
        assertTrue(headers.containsKey("Cookie"), "请求头应包含Cookie");
        assertTrue(headers.containsKey("User-Agent"), "请求头应包含User-Agent");
        assertTrue(headers.containsKey("Accept"), "请求头应包含Accept");
    }

    /**
     * 根据提供的curl命令测试抖音直播接口
     * 使用HttpClientUtil直接发送HTTP请求
     */
    @Test
    @Disabled("由于抖音反爬虫机制以及需要有效的Cookie，此测试可能无法正常运行")
    public void testDouyinLiveRequestWithCurlData() {
        try {
            // 设置URL
            String url = "https://live.douyin.com/webcast/room/web/enter/?aid=6383&app_name=douyin_web&live_id=1&device_platform=web&language=zh-CN&enter_from=web_live&cookie_enabled=true&screen_width=1536&screen_height=864&browser_language=zh-CN&browser_platform=Win32&browser_name=Chrome&browser_version=140.0.0.0&web_rid=349213906905&room_id_str=7548432746062973742&enter_source=&is_need_double_stream=false&insert_task_id=&live_reason=&msToken=4DxjcfsdvS9pah7xjncapOGqgwwbKYbc94-S1sZoyh0B4KOQw28Y9WP1fsb5aNn_xVSOz-YRwBtKT2DueFo9skoUudFvyjIR-wxNk3ACOLNIKOVRof3IerDVdxKYsrLwYhayyxnkH1imyUMSXr7HQNgUmBGhKgMlYusaqq8LR-fetA64zk02bg%3D%3D&a_bogus=dJsfhe6yd2A5OdMS8CnfSXqlguolrBSyqpi%2FSn5KexORc1lGe8PtDxcdGoK0mbQMnWBziq3HvfFAYdVcTdXT1%2F9kqmkkSQ46y4%2FCV68o8qwhGlT%2FDHfYeLYFqwsFUmiN-ACyi1m5Is0x2fxRVqVEABZat5FqQcjgbHB5p%2FG9cDCWpT6TV9dWC-fWVwE%3D";
            
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36");
            
            String cookie = "x-web-secsdk-uid=31b5abfb-ebb9-4f33-9dfb-2146559195df; has_avx2=null; " +
                    "device_web_cpu_core=8; device_web_memory_size=8; live_use_vvc=%22false%22; hevc_supported=true; " +
                    "xgplayer_user_id=432019988802; csrf_session_id=43adc6e45e0d15598f162241807438b2; " +
                    "fpk1=U2FsdGVkX1+6lj81VtZwBUxUP1MQ06LyW95TuMC/MdMN/WRu6rUmlzhbtXcWdecYB5mek38IemjnFfAW6JLVVg==; " +
                    "fpk2=a3f57bbe21c4e30379228ad7788f224d; enter_pc_once=1; " +
                    "UIFID_TEMP" +
                    "=05e13338fddfba3e77d97bdae5b40c997761c402860e341151ba0aeaadef04051497772c90c9cfbc34ac9e78083665579d3c4ee0b4ee9c4b8a05f854a50c9dfeb40d6d85f0ef7a8194321f62f214fc499858fbb50457bc6f50adf7aa284afd62; odin_tt=fb3000b23a0354c327e8081802e3459eed6a51aef0405b681f5eaf2a5fc8982b7dfdd4ace8202473a01380fad407e6f7ae0832b9c1597a57ab39973de8f31ac5b651d6c37af6ab771e68d12cca353621; passport_csrf_token=1328385544e3c552d980232580e236f5; passport_csrf_token_default=1328385544e3c552d980232580e236f5; is_dash_user=1; __security_mc_1_s_sdk_crypt_sdk=faac30c7-4839-a90a; bd_ticket_guard_client_web_domain=2; SEARCH_RESULT_LIST_TYPE=%22single%22; xgplayer_device_id=50775684878; download_guide=%223%2F20250908%2F0%22; webcast_leading_last_show_time=1757427686614; webcast_leading_total_show_times=2; webcast_local_quality=sd; h265ErrorNum=-1; UIFID=05e13338fddfba3e77d97bdae5b40c997761c402860e341151ba0aeaadef04053ea8c50ab97f3c7ed3c892707417f0e8c636103bbbbdd53d24d5993896abbeab5858c3fd163fd697bda1ab4accd1c9daa053f1a1accc8ea4239ecfd4f208ca74e7780d6287c2cee3a50b87fc1d0b95634b5543ebfeb1a933261137a8ec48b44a79de5cbb82c721383364676f44817841b07aa0fc5e1f6c524e289d6362e210df0a8e9b49933078083d94432fa4d33f361c94c96775aa66b8906163af6fb14791; strategyABtestKey=%221757460134.784%22; stream_player_status_params=%22%7B%5C%22is_auto_play%5C%22%3A0%2C%5C%22is_full_screen%5C%22%3A0%2C%5C%22is_full_webscreen%5C%22%3A0%2C%5C%22is_mute%5C%22%3A0%2C%5C%22is_speed%5C%22%3A1%2C%5C%22is_visible%5C%22%3A0%7D%22; stream_recommend_feed_params=%22%7B%5C%22cookie_enabled%5C%22%3Atrue%2C%5C%22screen_width%5C%22%3A1536%2C%5C%22screen_height%5C%22%3A864%2C%5C%22browser_online%5C%22%3Atrue%2C%5C%22cpu_core_num%5C%22%3A8%2C%5C%22device_memory%5C%22%3A8%2C%5C%22downlink%5C%22%3A10%2C%5C%22effective_type%5C%22%3A%5C%224g%5C%22%2C%5C%22round_trip_time%5C%22%3A200%7D%22; volume_info=%7B%22isUserMute%22%3Afalse%2C%22isMute%22%3Afalse%2C%22volume%22%3A0.5%7D; bd_ticket_guard_client_data=eyJiZC10aWNrZXQtZ3VhcmQtdmVyc2lvbiI6MiwiYmQtdGlja2V0LWd1YXJkLWl0ZXJhdGlvbi12ZXJzaW9uIjoxLCJiZC10aWNrZXQtZ3VhcmQtcmVlLXB1YmxpYy1rZXkiOiJCQUo0QlZzTUxsWU13bHNScmxzRmZGdUlHb3lUV3RlZDg0QUNkUXRJMGtpQXp4aGovVXNmNkpUZVRTV0wxUGFJUVVHTzF6TTdMNkp3UzdXa2Rpalh5SGc9IiwiYmQtdGlja2V0LWd1YXJkLXdlYi12ZXJzaW9uIjoyfQ%3D%3D; biz_trace_id=bde2ad3f; ttwid=" + ByteDanceUtil.getTtwid() + "s; home_can_add_dy_2_desktop=%221%22; WallpaperGuide=%7B%22showTime%22%3A1757428556224%2C%22closeTime%22%3A0%2C%22showCount%22%3A1%2C%22cursor1%22%3A22%2C%22cursor2%22%3A6%7D; __ac_signature=_02B4Z6wo00f01XB6JWQAAIDC.dKc7Z3c7UVwWiHAADTj14; __live_version__=%221.1.3.9849%22; xg_device_score=7.680383457615924; live_can_add_dy_2_desktop=%221%22; IsDouyinActive=false";
            headers.put("Cookie", cookie);
            
            System.out.println("请求URL: " + url);
            System.out.println("请求头数量: " + headers.size());
            
            // 使用HttpClientUtil发送GET请求
            String response = HttpClientUtil.get(url, headers);
            
            // 验证响应不为空
            assertNotNull(response, "响应内容不应为空");
            assertTrue(response.length() > 0, "响应内容应该包含数据");
            
            System.out.println("响应长度: " + response.length());
            System.out.println("响应预览: " + (response.length() > 200 ? response.substring(0, 200) : response));
            
        } catch (Exception e) {
            fail("请求抖音直播接口时发生异常: " + e.getMessage(), e);
        }
    }
}