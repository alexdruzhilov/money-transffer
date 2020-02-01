package com.revolut.moneytransfer.database;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

@Factory
@RequiredArgsConstructor
public class MybatisFactory {
    private final DataSource dataSource;

    @Bean
    SqlSessionFactory sqlSessionFactory() {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();

        Environment environment = new Environment("dev", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMappers("com.revolut.moneytransfer.database");

        return new SqlSessionFactoryBuilder().build(configuration);
    }
}
