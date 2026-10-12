package com.psy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 校园心理健康智能测评与随访系统启动类
 */
@SpringBootApplication
@MapperScan("com.psy.**.mapper")
public class PsyTestApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(PsyTestApplication.class, args);
        String port = context.getEnvironment().getProperty("server.port");
        System.out.println("当前正在使用的端口号：" + port);
        System.out.println("校园心理健康智能测评与随访系统后端启动成功，访问地址：http://localhost:" + port);
    }
}
