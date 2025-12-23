package com.back.matchduo.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MatchDuo API")
                        .description("리그 오브 레전드 듀오 찾기 플랫폼 API")
                        .version("v1.0"))
                .servers(List.of(
                        new Server()
                                .url("https://api.matchmyduo.shop")
                                .description("운영 서버"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 서버")
                ));
    }
}