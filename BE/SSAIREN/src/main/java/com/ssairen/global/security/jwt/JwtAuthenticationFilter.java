package com.ssairen.global.security.jwt;

import com.ssairen.global.exception.CustomException;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * JWT 인증 필터
 * 모든 요청에서 JWT를 검증하고 SecurityContext에 인증 정보를 저장
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Authorization 헤더에서 JWT 추출
            String token = extractToken(request);

            // 2. JWT가 있으면 검증 및 인증 처리
            if (token != null) {
                authenticateUser(token);
            }

        } catch (CustomException e) {
            // JWT 검증 실패 시 로그만 남기고 계속 진행 (AuthenticationEntryPoint에서 처리)
            log.warn("JWT authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        // 3. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 JWT 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * JWT를 검증하고 SecurityContext에 인증 정보 저장
     *
     * @param token JWT 토큰
     */
    private void authenticateUser(String token) {
        // 1. JWT 검증 및 파싱
        Claims claims = jwtTokenProvider.parseToken(token);

        // 2. Claims에서 사용자 정보 추출 (중복 파싱 로직 제거)
        JwtTokenProvider.UserInfo userInfo = jwtTokenProvider.extractUserInfoFromClaims(claims);

        // 3. Claims에서 authorities 추출
        String authoritiesStr = claims.get("authorities", String.class);
        Collection<? extends GrantedAuthority> authorities = parseAuthorities(authoritiesStr);

        // 4. CustomUserPrincipal 재구성
        CustomUserPrincipal principal = new CustomUserPrincipal(
                userInfo.getUserId(),
                userInfo.getUsername(),
                null,  // 비밀번호는 포함하지 않음
                userInfo.getUserType(),
                authorities
        );

        // 5. Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );

        // 6. SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Successfully authenticated user: userType={}, userId={}, username={}",
                userInfo.getUserType(), userInfo.getUserId(), userInfo.getUsername());
    }

    /**
     * authorities 문자열을 GrantedAuthority 컬렉션으로 변환
     *
     * @param authoritiesStr "ROLE_PARAMEDIC,ROLE_USER" 형태
     * @return GrantedAuthority 컬렉션
     * @throws CustomException authorities가 없는 경우 (잘못된 토큰)
     */
    private Collection<? extends GrantedAuthority> parseAuthorities(String authoritiesStr) {
        if (!StringUtils.hasText(authoritiesStr)) {
            log.error("JWT token does not contain authorities claim");
            throw new CustomException(
                    com.ssairen.global.exception.ErrorCode.INVALID_TOKEN,
                    "토큰에 권한 정보가 없습니다"
            );
        }

        return Arrays.stream(authoritiesStr.split(","))
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
