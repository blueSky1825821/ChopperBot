package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.bean.AccountCookie;

/**
 * @author wangmin
 * @version 1.0
 * @description 只是为了获取cookie，方便爬数据，不应该查询account的，或者单独维护一个cookie表
 * @date 2025/9/10 22:51
 */
public interface AccountCookieMapper extends BaseMapper<AccountCookie> {
}
