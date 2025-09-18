package org.example.core.constpool;

import lombok.Getter;

import java.util.Objects;

/**
 * @Description
 * @Author welsir
 * @Date 2023/10/12 20:47
 */
public class ConstPool {

    //账号平台
    @Getter
    public enum AccountPlatForm{

        BILIBILI("1"),
        DOUYIN("2");

        private String id;

        AccountPlatForm(String id){
            this.id = id;
        }

        public static int getPlatFormId (int id){
            return id;
        }
        public static String fromId(String id) {
            for (AccountPlatForm platform : values()) {
                if (Objects.equals(platform.id, id)) {
                    return platform.toString();
                }
            }
            throw new IllegalArgumentException("Invalid id: " + id);
        }
    }

}
