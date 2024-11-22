package org.zerock.Altari.Kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zerock.Altari.dto.UserDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserMedicationTimeEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.UserMedicationTimeRepository;
import org.zerock.Altari.repository.UserProfileRepository;
import org.zerock.Altari.repository.UserRepository;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.UserService;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class OAuthService {

    private final UserProfileRepository userProfileRepository;
    private final UserMedicationTimeRepository userMedicationTimeRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    public String kakao_redirect_uri;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    public String kakao_client_id;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> kakaoLogin(String authorizationCode) {
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
//
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> userInfo = response.getBody();
            log.info("카카오 사용자 정보: " + userInfo);


            // 프로필 정보 추출
            String id = userInfo.get("id").toString();
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String nickname = profile.get("nickname").toString();
            String profileImageUrl = profile.get("profile_image_url").toString();
            String email = kakaoAccount.get("email").toString();
//
            String password = passwordEncoder.encode(id);
//            // 사용자 정보가 DB에 있는지 확인하고 없으면 새로 저장

            boolean isNewUser = false;

            UserEntity userEntity = userRepository.findByUsername(email);
            if (userEntity == null) {

                isNewUser = true; // 새로운 회원임을 표시

                userEntity = UserEntity.builder()
                        .username(email)
                        .password(password)
                        .role("USER")
                        .build();
                UserEntity user = userRepository.save(userEntity);

                UserProfileEntity userProfile = UserProfileEntity.builder()
                        .username(user)
                        .fullName(nickname)
                        .profileImage(profileImageUrl)
                        .morningMedicationTime(LocalTime.parse("10:00"))
                        .lunchMedicationTime(LocalTime.parse("14:00"))
                        .dinnerMedicationTime(LocalTime.parse("19:00"))
                        .build();

                userProfileRepository.save(userProfile);

                UserMedicationTimeEntity userMedicationTime = UserMedicationTimeEntity.builder()
                        .onMorningMedicationAlarm(true)
                        .onLunchMedicationTimeAlarm(true)
                        .onDinnerMedicationTimeAlarm(true)
                        .onNightMedicationTimeAlarm(true)
                        .userProfile(userProfile)
                        .build();

                userMedicationTimeRepository.save(userMedicationTime);
            }


            UserDTO userDTOResult = userService.read(email, id); //

            log.info(userDTOResult);

            String username = userDTOResult.getUsername(); //

            Map<String, Object> dataMap = userDTOResult.getDataMap();

            String jwtToken = jwtUtil.createToken(dataMap, 60);

            String refreshToken = jwtUtil.createToken(Map.of("username", username), 60 * 24 * 30); //

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", jwtToken);
            responseData.put("refreshToken", refreshToken);

            if (isNewUser) {
                responseData.put("message", "Welcome! New User Registered");
                responseData.put("isNewUser", true); // 새로운 회원 여부를 응답에 추가
            } else {
                responseData.put("message", "Welcome Back! Existing User");
                responseData.put("isNewUser", false); // 기존 회원 여부를 응답에 추가
            }

            return CompletableFuture.completedFuture(
                    ResponseEntity.ok(responseData));
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
                + "redirect_uri="+kakao_redirect_uri+"&"  // 리디렉트 URI
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