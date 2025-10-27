package com.ssairen.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI(Swagger) 설정
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SSAIREN API")
                        .version("1.0")
                        .description("SSAFY 13기 A205팀 프로젝트 API 문서"))
                .servers(List.of(
                        new Server()
                                .url("https://be.ssairen.site")
                                .description("Production Server (HTTPS)")
                ));
    }
}
