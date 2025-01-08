package org.zerock.Altari.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
//import org.zerock.ex3.member.security.auth.CustomUserPrincipal;
import org.springframework.stereotype.Component;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.repository.UserRepository;
import org.zerock.Altari.security.auth.CustomUserPrincipal;
import org.zerock.Altari.security.util.JWTUtil;

import java.util.Arrays;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Log4j2
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
//    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // 기존 경로 `/api/v1/token/` 외에도 `/altari/`와 `/login/oauth2/code/` 경로 추가
        return path.startsWith("/api/v1/token/") || path.equals("/login/oauth2/code/kakao") || path.equals("/altari/kakao/login");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 토큰을 검증하여 문제가 없을 경우 컨트롤러 혹은 필터가 작동하도록 설정

        log.info("JWTCheckFilter doFilter..........");
        log.info("requestURI: " + request.getRequestURI());

        if (request.getRequestURI().equals("/altari/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/altari/kakao/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/altari/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/altari/check/username")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/altari/test")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/login/oauth2/code/kakao")) {
            filterChain.doFilter(request, response);
            return;
        }



        // 특정 엔드포인트에 대한 접근 제한 해제

        String headerStr = request.getHeader("Authorization");
        log.info("headerStr: " + headerStr);

        if (headerStr == null || !headerStr.startsWith("Bearer ")) {
            handleException(response, new Exception("ACCESS TOKEN NOT FOUND"));
            return;
        }


        String accessToken = headerStr.substring(7);

        try {
            // 1. JWT 토큰 검증 및 정보 추출
            java.util.Map<String, Object> tokenMap = jwtUtil.validateToken(accessToken);
            log.info("tokenMap: " + tokenMap);

            String username = tokenMap.get("username").toString();

            // 2. 데이터베이스에서 사용자 정보 및 역할 조회
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // 3. 스프링 시큐리티 권한 객체로 변환
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                    .collect(Collectors.toList());

            // 4. 인증 객체 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            new CustomUserPrincipal(username), // 사용자 정보
                            null, // 비밀번호는 null
                            authorities // 권한 리스트
                    );

            // 5. 인증 정보 설정
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handleException(response, e);
        }

    }
//
    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().println("{\"error\":\"" + e.getMessage() + "\"}");
    }
}
