package org.example.core.creeper.loadtask;

import org.example.bean.live.DouyinLive;
import org.example.constpool.ConstPool;
import org.example.core.creeper.loadconfig.DouyinLiverCheckerConfig;
import org.example.core.creeper.processor.DouyinLiverCheckerProcessor;
import org.example.core.factory.SpiderFactory;
import org.example.core.loadtask.WebMagicLoadTask;
import org.example.log.ChopperLogFactory;
import org.example.log.LoggerType;
import org.slf4j.Logger;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

/**
 * @author dhx
 * @date 2024/5/26 11:08
 */
public class DouyinLiverCheckerLoadTask extends WebMagicLoadTask<DouyinLive> {
    private static final Logger LOGGER = ChopperLogFactory.getLogger(LoggerType.Hot);

    private DouyinLiverCheckerConfig config;
    public DouyinLiverCheckerLoadTask(DouyinLiverCheckerConfig loadConfig) {
        super(loadConfig);
        this.config = loadConfig;
    }

    @Override
    public DouyinLive start() {
        Request request = new Request(config.getUrl());
        request.addHeader("Cookie", config.getCookieStr());
        request.addHeader("User-Agent", config.getUserAgent());

        DouyinLive live;

        Spider spider = SpiderFactory.buildSpider(
                ConstPool.PLATFORM.DOUYIN.getName(),
                new DouyinLiverCheckerProcessor(),
                request
        );
        try {
            live = getData(spider, config.getUrl());
        }catch (Exception e){
            LOGGER.error("获取直播信息出错, url:{}", config.getUrl());
            return null;
        }
        return live;
    }

    @Override
    public void end() {

    }
}