package org.example.core.creeper.loadconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Getter;
import lombok.Setter;
import org.example.bean.FocusLiver;
import org.example.constpool.ConstGroup;
import org.example.constpool.ConstPool;
import org.example.core.creeper.loadtask.DouyinLiveOnlineLoadTask;
import org.example.core.manager.Creeper;
import org.example.log.ChopperLogFactory;
import org.example.log.LoggerType;
import org.example.service.FocusLiverService;
import org.example.util.SpringBeanUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author dhx
 * @date 2024/5/19 15:33
 */
@Getter
@Setter
@Creeper(creeperName = "抖音直播爬虫",
        loadTask = DouyinLiveOnlineLoadTask.class,
        creeperDescription = "抖音直播爬取(包含监控器)",
        priority = 10,
        group = ConstGroup.LIVE_ONLINE,
        platform = ConstPool.DOUYIN
)
public class DouyinLiveOnlineConfig extends LoadLiveConfig {
    private static final Logger LOGGER = ChopperLogFactory.getLogger(LoggerType.LiveRecord);
    private String cookieStr;

    public DouyinLiveOnlineConfig(String roomId, String videoPath, String videoName,int clarity) {
        super(roomId, videoPath, videoName, false);
        this.platform = ConstPool.PLATFORM.DOUYIN.getName();
        setHeader();
    }

    public DouyinLiveOnlineConfig(String roomId, String videoPath, String videoName,boolean convertToMp4,int clarity) {
        super(roomId, videoPath, videoName, convertToMp4);
        this.platform = ConstPool.PLATFORM.DOUYIN.getName();
        setHeader();
    }

    public DouyinLiveOnlineConfig(String roomId, String videoPath, String videoName,boolean convertToMp4) {
        super(roomId, videoPath, videoName, convertToMp4);
        this.platform = ConstPool.PLATFORM.DOUYIN.getName();
        setHeader();
    }

    private void setHeader(){
        FocusLiverService focusLiverService = SpringBeanUtils.getBean(FocusLiverService.class);
        QueryWrapper<FocusLiver> query = new QueryWrapper<>();
        query.eq("room_id_str", roomId);//直播间id 非房间号
        FocusLiver focusLiver = focusLiverService.getOne(query);
        if (Objects.isNull(focusLiver)) {
            LOGGER.warn("未找到该主播信息");
            return;
        }
        JSONObject extJson = JSON.parseObject(focusLiver.getExt());
        setUrl(extJson.getString("enterUrl"));
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", extJson.getString("Cookie"));
        headers.put("User-Agent", extJson.getString("User-Agent"));
        setHeader(headers);

        this.Origin = "https://live.douyin.com";
        this.Referer = "https://live.douyin.com";
    }
}
