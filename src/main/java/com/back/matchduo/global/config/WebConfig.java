package com.back.matchduo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/images/**"로 시작하는 URL로 요청이 오면
        // 실제 하드디스크의 "C:/matchduo_uploads/" 폴더에서 파일을 찾아라!
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/matchduo_uploads/");
        // 리눅스 배포 시에는 "file:/home/ubuntu/uploads/" 처럼 바뀝니다.
    }
}