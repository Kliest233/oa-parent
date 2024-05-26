package com.atguigu.config.mp;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: MybatisPlusConfig
 * Package: com.atguigu.config.mp
 * Description:
 *
 * @Author Klilest
 * @Create 2024/5/10 17:02
 * @Version 1.0
 */
@MapperScan(basePackages = {"com.atguigu.auth.mapper","com.atguigu.process.mapper","com.atguigu.wechat.mapper"})
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor=new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer(){
        return configuration -> configuration.setUseDeprecatedExecutor(false);
    }
}
