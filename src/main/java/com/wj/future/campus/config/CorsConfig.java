package com.wj.future.campus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许所有接口
                .allowedOriginPatterns("*") // 允许前端域名
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许方法
                .allowCredentials(true) // 是否允许带 cookie
                .maxAge(3600); // 缓存预检请求时间（秒）
    }
}
