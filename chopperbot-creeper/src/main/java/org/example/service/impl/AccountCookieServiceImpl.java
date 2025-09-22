package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.example.bean.AccountCookie;
import org.example.mapper.AccountCookieMapper;
import org.example.service.AccountCookieService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author wangmin
 * @version 1.0
 * @description TODO
 * @date 2025/9/10 22:57
 */
@Service
public class AccountCookieServiceImpl extends ServiceImpl<AccountCookieMapper, AccountCookie> implements AccountCookieService {
    @Resource
    private AccountCookieMapper accountCookieMapper;

    @Override
    public AccountCookie getOneAccountCookie(String platformId) {
        QueryWrapper<AccountCookie> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("platform_id", platformId);
        List<AccountCookie> accountCookies = accountCookieMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(accountCookies)) {
            return accountCookies.get(0);
        }
        return null;
    }
}
