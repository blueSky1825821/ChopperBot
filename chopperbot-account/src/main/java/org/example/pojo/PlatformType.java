package org.example.pojo;

import java.util.Objects;

/**
 * @Description
 * @Author welsir
 * @Date 2023/9/24 19:28
 */
public enum PlatformType {

    BILIBILI("1"),
    DOUYIN("2");
    private final String index;

    public String getId() {
        return index;
    }

    public static PlatformType getPlatform(String id) {
        for (PlatformType platform : values()) {
            if (Objects.equals(platform.index, id)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Invalid platform id: " + id);
    }

    PlatformType(String index){
        this.index = index;
    }

}
