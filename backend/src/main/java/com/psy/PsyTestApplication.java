package com.psy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 区域智能诊疗辅助诊断系统启动类
 */
@SpringBootApplication
@MapperScan("com.psy.**.mapper")
public class PsyTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsyTestApplication.class, args);
        System.out.println("区域智能诊疗辅助诊断系统后端启动成功，访问地址：http://localhost:8080");
    }
}
