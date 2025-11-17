package com.ssairen.config;

import org.springframework.context.annotation.Configuration;

/**
 * Actuator 보안 설정
 * 프로덕션 환경에서는 Spring Security를 통해 엔드포인트를 보호해야 합니다.
 */
@Configuration
public class ActuatorSecurityConfig {

    // TODO: 프로덕션 환경에서는 Spring Security 설정 추가 필요
    // 예시:
    // @Bean
    // public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
    //     http
    //         .securityMatcher("/actuator/**")
    //         .authorizeHttpRequests(auth -> auth
    //             .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
    //             .requestMatchers("/actuator/info").permitAll()
    //             .requestMatchers("/actuator/**").hasRole("ADMIN")
    //         );
    //     return http.build();
    // }
}