package com.atguigu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * ClassName: ServiceAuthApplication
 * Package: com.atguigu.auth
 * Description:
 *
 * @Author Klilest
 * @Create 2024/5/10 14:20
 * @Version 1.0
 */
@SpringBootApplication
@ComponentScan("com.atguigu")
public class ServiceAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceAuthApplication.class);
    }
}
