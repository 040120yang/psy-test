package com.psy.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.Collections;

/**
 * Knife4j 在线接口文档配置
 * 启动后端后访问 http://localhost:8080/doc.html
 * 说明：Spring Boot 2.6+ 需在 application.yml 配置
 *   spring.mvc.pathmatch.matching-strategy: ant_path_matcher
 */
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("区域智能诊疗辅助诊断系统 API")
                        .description("前后端分离架构，参照若依（RuoYi-Vue）接口风格；调试需先登录获取令牌并填入 Authorization 请求头。")
                        .version("1.0.0")
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.psy"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Collections.singletonList(new ApiKey("BearerAuth", "Authorization", "header")))
                .securityContexts(Collections.singletonList(SecurityContext.builder()
                        .securityReferences(Collections.singletonList(new SecurityReference("BearerAuth",
                                new AuthorizationScope[]{new AuthorizationScope("global", "accessEverything")})))
                        .build()));
    }
}
