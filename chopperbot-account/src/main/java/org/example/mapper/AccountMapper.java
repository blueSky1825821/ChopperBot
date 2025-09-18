package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.pojo.Account;
import org.example.pojo.AccountType;
import org.example.sql.annotation.SQLInit;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description
 * @Author welsir
 * @Date 2023/9/23 14:15
 */
public interface AccountMapper extends BaseMapper<Account> {

    @Select("select * from account a join account_type pf on a.platform_id=pf.type where pf.type = #{id}")
    List<Account> selectUserByPlatform(String id);

    @Select("select * from account_type where uid=#{id}")
    List<AccountType> selectTypeByUid(Long id);

    // 根据用户名和平台ID更新账号
    @Update("UPDATE account SET cookie = #{cookie} WHERE username = #{username} AND platform_id =" +
            " #{platformId}")
    int updateByUsernameAndPlatformId(Account account);

    // 根据用户名和平台ID查找账号
    @Select("SELECT * FROM account WHERE username = #{username} AND platform_id = #{platformId}")
    Account selectByUsernameAndPlatformId(@Param("username") String username, @Param("platformId") String platformId);
}