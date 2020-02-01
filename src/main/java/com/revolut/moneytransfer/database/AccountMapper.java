package com.revolut.moneytransfer.database;

import com.revolut.moneytransfer.model.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface AccountMapper {
    @Select("select * from account where id=#{id}")
    Account findById(String id);

    @Insert("insert into account (id, name, balance, currency)\n" +
            "values (#{account.id}, #{account.name}, #{account.balance}, #{account.currency})")
    void createAccount(@Param("account") Account account);

    @Update("update account set balance = #{balance}\n" +
            "where id = #{accountId}")
    void updateBalance(@Param("accountId") String accountId,
                       @Param("balance") long balance);
}
