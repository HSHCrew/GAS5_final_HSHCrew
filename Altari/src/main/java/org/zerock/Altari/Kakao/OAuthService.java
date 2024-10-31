package org.zerock.Altari.Kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.repository.UserProfileRepository;
import org.zerock.Altari.repository.UserRepository;
import org.zerock.Altari.security.util.JWTUtil;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class OAuthService {

    private final UserProfileRepository userProfileRepository;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    public String kakao_redirect_uri;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    public String kakao_client_id;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

    public String kakaoLogin(String authorizationCode) {
        // Access Token 요청
        String accessToken = getAccessToken(authorizationCode);

        // 카카오 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                KAKAO_USERINFO_URL,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> userInfo = response.getBody();
            log.info("카카오 사용자 정보: " + userInfo);

            // 사용자 ID 추출
            String id = userInfo.get("id").toString();

            // 사용자 정보가 DB에 있는지 확인하고 없으면 새로 저장
            UserEntity userEntity = userRepository.findByUsername(id);
            if (userEntity == null) {
                userEntity = UserEntity.builder()
                        .username(id)
                        .build();
                UserEntity user = userRepository.save(userEntity);

                UserProfileEntity userProfile = UserProfileEntity.builder()
                        .username(user)
                        .build();
                userProfileRepository.save(userProfile);
            }


            // JWT 생성
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", id);  // 'username' 클레임에 닉네임을 추가

            // 기존 createToken 메서드 사용
            String jwtToken = jwtUtil.createToken(claims, 1440);  // 토큰 유효 시간(분) 설정

            return jwtToken;
        } else {
            throw new RuntimeException("카카오 사용자 정보 조회 실패");
        }
    }

    private String getAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 카카오 API 요청에 필요한 파라미터
        String requestBody = "grant_type=authorization_code&"
                + "client_id="+kakao_client_id+"&"  // 카카오 REST API 키
                + "redirect_uri="+kakao_redirect_uri+"&"  // 리다이렉트 URI
                + "code=" + authorizationCode;

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                KAKAO_TOKEN_URL,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("Access Token 요청 실패");
        }
    }
}
