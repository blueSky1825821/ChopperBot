package org.example.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.constpool.ConstCreeper;

/**
 * @author Genius
 * @date 2023/08/06 21:56
 **/

@Data
@AllArgsConstructor
public class SpiderConfig {
    private String userAgent;
    private int retryTimes;
    private int retrySleepTime;
    private int threadCnt;
    private int emptySleepTime;
    private int sleepTime;

    public SpiderConfig() {
        userAgent = ConstCreeper.USER_AGENT;
        retryTimes = 3;
        retrySleepTime = 1000;
        threadCnt = 1;
        emptySleepTime = 1000;
        sleepTime = 100;
    }
}
