package com.future.campus.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@MapperScan("com.wj.future.campus.mapper") // 替换为你的 mapper 包路径
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 【关键】指定数据库类型为 POSTGRE_SQL
        // 这会确保分页插件使用 PG 的语法，而不是 MySQL 的语法
        interceptor.addInnerInterceptor(new InnerInterceptor() {
             @Override
            public void setProperties(Properties properties) {
                 properties.setProperty("dbType", DbType.POSTGRE_SQL.name());
             }
        });
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));

        return interceptor;
    }
}