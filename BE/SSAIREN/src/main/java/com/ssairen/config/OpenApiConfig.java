package com.ssairen.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
        // JWT Bearer Token 인증 방식 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("SSAIREN API")
                        .version("1.0")
                        .description("SSAFY 13기 A205팀 프로젝트 API 문서\n\n" +
                                "## 인증 방법\n" +
                                "1. `/api/auth/login` 엔드포인트로 로그인\n" +
                                "2. 응답에서 받은 `accessToken`을 복사\n" +
                                "3. 우측 상단 '🔒 Authorize' 버튼 클릭\n" +
                                "4. `Bearer {accessToken}` 형식으로 입력 (Bearer 제외하고 토큰만 입력)\n" +
                                "5. 'Authorize' 클릭\n\n" +
                                "**테스트 계정:**\n" +
                                "- 구급대원: `userType=PARAMEDIC`, `username=20240001`, `password=Password123!`\n" +
                                "- 병원: `userType=HOSPITAL`, `username=서울대`, `password=Password123!`"))
                .servers(List.of(
                        new Server()
                                .url("https://api.ssairen.site")
                                .description("Production API Server (HTTPS)"),

                        new Server()
                                .url("https://be.ssairen.site")
                                .description("Production Server (HTTPS)"),

                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
