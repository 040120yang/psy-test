package com.psy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园心理健康智能测评与随访系统启动类
 */
@SpringBootApplication
@MapperScan("com.psy.**.mapper")
public class PsyTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsyTestApplication.class, args);
        System.out.println("校园心理健康智能测评与随访系统后端启动成功，访问地址：http://localhost:8081");
    }
}
