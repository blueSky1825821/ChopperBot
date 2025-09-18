package org.example.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wangmin
 * @version 1.0
 * @description TODO
 * @date 2025/9/10 22:52
 */
@TableName(value = "account")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCookie implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "uid", type = IdType.ASSIGN_ID)
    private Long uid;

    @TableField("username")
    private String username;

    @TableField("cookies")
    private String cookies;

    @TableField("is_complete_match")
    private boolean isCompleteMatch;

    @TableField("platform_id")
    private String platformId;
}
