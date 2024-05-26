package com.atguigu.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat") //表示读取配置文件
public class WechatAccountConfig {

    private String mpAppId;

    private String mpAppSecret;
}
