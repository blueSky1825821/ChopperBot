package org.example.core.parser.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.example.core.creeper.loadconfig.DouyinLiveOnlineConfig;
import org.example.core.parser.PlatformVideoUrlParser;
import org.example.log.ChopperLogFactory;
import org.example.log.LoggerType;
import org.example.util.HttpClientUtil;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author dhx
 * @date 2024/5/19 15:32
 */
public class DouyinFlvUrlParser implements PlatformVideoUrlParser<DouyinLiveOnlineConfig> {
    private static final Logger LOGGER = ChopperLogFactory.getLogger(LoggerType.LiveRecord);

    @Override
    public String getUrl(DouyinLiveOnlineConfig loadConfig) throws Exception {
        String url = loadConfig.getUrl();
        String roomId = loadConfig.getRoomId();
        Map<String,String> header = loadConfig.getHeader();
        try {
//            header.put("Cookie",String.format("ttwid=%s;",ByteDanceUtil.getTtwid()));
            String resp = HttpClientUtil.get(url, header);
            JSONObject jsonObject = JSON.parseObject(resp);
            if(jsonObject==null||jsonObject.getJSONObject("data")==null)return null;
            return (String) jsonObject.getJSONObject("data").getJSONArray("data").getJSONObject(0).getJSONObject("stream_url").getJSONObject("flv_pull_url").get("FULL_HD1");
        } catch (Exception e){
            LOGGER.error("获取抖音直播地址失败,url:{},roomId:{}, e:", url, roomId, e);
            return null;
        }
    }
}
