package org.example.core.creeper.loadconfig;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Getter;
import lombok.Setter;
import org.example.bean.FocusLiver;
import org.example.constpool.ConstGroup;
import org.example.constpool.ConstPool;
import org.example.core.creeper.loadtask.DouyinLiverCheckerLoadTask;
import org.example.core.loadconfig.LoadConfig;
import org.example.core.manager.Creeper;
import org.example.log.ChopperLogFactory;
import org.example.log.LoggerType;
import org.example.service.FocusLiverService;
import org.example.util.SpringBeanUtils;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * @author dhx
 * @date 2024/5/26 10:56
 */
@Getter
@Setter
@Creeper(creeperName = "抖音直播检测爬虫",
        loadTask = DouyinLiverCheckerLoadTask.class,
        creeperDescription = "用于检测抖音主播是否开播，并且获取直播详细信息",
        priority = 10,
        group = ConstGroup.LIVER_CHECKER,
        platform = ConstPool.DOUYIN
)
public class DouyinLiverCheckerConfig extends LoadConfig {
    private static final Logger LOGGER = ChopperLogFactory.getLogger(LoggerType.Hot);

    private String roomId;
    private String cookieStr;

    public DouyinLiverCheckerConfig(String roomId) {
        setRoomId(roomId);
        FocusLiverService focusLiverService = SpringBeanUtils.getBean(FocusLiverService.class);
        QueryWrapper<FocusLiver> query = new QueryWrapper<>();
        query.eq("room_id", roomId);
        FocusLiver focusLiver = focusLiverService.getOne(query);
        if (Objects.isNull(focusLiver)) {
            LOGGER.warn("未找到该主播信息");
            return;
        }
        JSONObject extJSON = JSON.parseObject(focusLiver.getExt());
        setUrl(extJSON.getString("enterUrl"));
        setCookieStr(extJSON.getString("Cookie"));
        setUserAgent(extJSON.getString("User-Agent"));
    }


    @Override
    public String getTaskId() {
        return super.getTaskId()+"_"+roomId;
    }
}
