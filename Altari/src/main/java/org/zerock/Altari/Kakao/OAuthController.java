package org.zerock.Altari.Kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.security.util.JWTUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/altari")
@RequiredArgsConstructor
@Log4j2
public class OAuthController {

    private final OAuthService oAuthService;

    // 카카오 로그인 엔드포인트
    // 해당 url로 get 요청 (YOUR_CLIENT_ID, redirect_uri 삽입 필요)-> https://kauth.kakao.com/oauth/authorize?client_id={YOUR_CLIENT_ID 앱키를 입력하세요}&response_type=code&redirect_uri={redirectURI를 입력하세요}
    // http://localhost:8080/login/oauth2/code/kakao?code=YOUR_AUTHORIZATION_CODE 해당 주소로 리디렉션
    // YOUR_AUTHORIZATION_CODE를 code 파라미터로 전달하며 호출
    @PostMapping("/kakao/login")
    public ResponseEntity<Map<String, Object>> kakaoLogin(@RequestParam("code") String authorizationCode) {
        try {
            // CompletableFuture 작업 완료 대기
            CompletableFuture<ResponseEntity<Map<String, Object>>> jwtTokenFuture = oAuthService.kakaoLogin(authorizationCode);

            // CompletableFuture에서 결과 가져오기
            ResponseEntity<Map<String, Object>> jwtTokenResponse = jwtTokenFuture.get();
            Map<String, Object> tokenMap = jwtTokenResponse.getBody(); // 반환된 Map 객체 가져오기

            // Map에서 필요한 값 추출
            String accessToken = (String) tokenMap.get("accessToken");
            String refreshToken = (String) tokenMap.get("refreshToken");
            boolean isNewUser = (boolean) tokenMap.get("isNewUser"); // 새 사용자 여부 가져오기

            // 최종 Map 생성 후 반환
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("accessToken", accessToken);
            responseMap.put("refreshToken", refreshToken);
            responseMap.put("isNewUser", isNewUser);

            return ResponseEntity.ok(responseMap);

        } catch (ExecutionException | InterruptedException e) {
            log.error("카카오 로그인 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "카카오 로그인 실패"));
        }
    }
}
