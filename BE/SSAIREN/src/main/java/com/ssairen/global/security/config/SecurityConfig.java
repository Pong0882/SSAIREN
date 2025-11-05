package com.ssairen.global.security.config;

import com.ssairen.global.security.handler.CustomAccessDeniedHandler;
import com.ssairen.global.security.handler.CustomAuthenticationEntryPoint;
import com.ssairen.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ROLE_PARAMEDIC = "PARAMEDIC";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /**
     * SecurityFilterChain 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용하므로 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 사용 안 함 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 엔드포인트별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 엔드포인트
                        .requestMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/logout").authenticated()  // 로그아웃은 인증 필요
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // WebSocket 엔드포인트 허용
                        .requestMatchers("/ws/**").permitAll()

                        // 출동 지령 관련 (로그인 불필요 - 119 시스템에서 호출)
                        .requestMatchers("/api/dispatches/**").permitAll()

                        // 구급대원 전용 엔드포인트
                        .requestMatchers("/api/paramedics/**").hasRole(ROLE_PARAMEDIC)
                        .requestMatchers("/api/emergency/**").hasRole(ROLE_PARAMEDIC)  // ������������ ������
                        .requestMatchers("/api/dispatches/**").hasRole(ROLE_PARAMEDIC)  // ������ ������ ������

                        // 병원 선택 요청 관련 (더 구체적인 패턴 먼저)
                        .requestMatchers("/api/hospital-selection/request").hasRole(ROLE_PARAMEDIC)  // ������ ������
                        .requestMatchers("/api/hospital-selection/ai-recommendation").hasRole(ROLE_PARAMEDIC)  // AI 병원 추천
                        .requestMatchers("/api/hospital-selection/**").hasRole("HOSPITAL")  // 요청 응답

                        // 병원 전용 엔드포인트
                        .requestMatchers("/api/hospitals/**").hasRole("HOSPITAL")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 예외 처리 (GlobalExceptionHandler와 동일한 형식 사용)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)  // 인증 실패
                        .accessDeniedHandler(customAccessDeniedHandler)            // 권한 부족
                )

                // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     * - 로컬 개발 환경: http://localhost:3000
     * - 배포 환경: https://ssairen.site, https://www.ssairen.site
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 Origin 설정
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",           // 로컬 개발
                "https://ssairen.site",            // 배포 환경
                "https://www.ssairen.site",        // 배포 환경 (www 포함)
                "https://api.ssairen.site",        // API 서버 (Swagger UI)
                "https://be.ssairen.site"          // Backend 서버 (Swagger UI)
        ));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 허용할 헤더
        config.setAllowedHeaders(Arrays.asList("*"));

        // 자격증명(쿠키, Authorization 헤더 등) 허용
        config.setAllowCredentials(true);

        // preflight 요청 캐싱 시간 (1시간)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    /**
     * 비밀번호 암호화 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
