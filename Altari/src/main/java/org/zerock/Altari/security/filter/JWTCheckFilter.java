package org.zerock.Altari.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
//import org.zerock.ex3.member.security.auth.CustomUserPrincipal;
import org.springframework.stereotype.Component;
import org.zerock.Altari.security.auth.CustomUserPrincipal;
import org.zerock.Altari.security.util.JWTUtil;

import java.util.Arrays;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
@Log4j2
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
//    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // JWTCheckFilter가 동작하지 않아야 하는 경로 지정을 위해 사용
        if (request.getServletPath().startsWith("/api/v1/token/")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 토큰을 검증하여 문제가 없을 경우 컨트롤러 혹은 필터가 작동하도록 설정

        log.info("JWTCheckFilter doFilter..........");
        log.info("requestURI: " + request.getRequestURI());

        if (request.getRequestURI().equals("/api/v1/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/api/v1/drugs")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/api/v1/check-username")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/api/codef/first")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().equals("/api/codef/second")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/api/v1/medications")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/api/v1/update-userProfile/test2")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().equals("/api/v1/get-userProfile/test2")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().equals("/api/v1/get-userHealth/test2")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().equals("/api/v1/update-userHealth/test2")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().equals("/api/v1/get-userPrescription/test2")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().equals("/oauth/kakao/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().equals("/api/v1/delete/user2")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().equals("/api/kakao/send")) {
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
            java.util.Map<String, Object> tokenMap = jwtUtil.validateToken(accessToken);
            log.info("tokenMap: " + tokenMap);

            String username = tokenMap.get("username").toString(); // auth_id를 username으로 변경
            String[] roles = tokenMap.get("role").toString().split(",");

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(new CustomUserPrincipal(username),
                            null,
                            Arrays.stream(roles)
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList())
                    );

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
