package org.zerock.Altari.Kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.security.util.JWTUtil;
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
    public ResponseEntity<Map<String, String>> kakaoLogin(@RequestParam("code") String authorizationCode) {
        try {
            CompletableFuture<ResponseEntity<Map<String, String>>> jwtTokenFuture = oAuthService.kakaoLogin(authorizationCode);
            ResponseEntity<Map<String, String>> jwtToken = jwtTokenFuture.get(); // 비동기 작업이 완료될 때까지 기다림
            Map<String, String> tokenMap = jwtToken.getBody();  // Map<String, String> 반환
            String accessToken = tokenMap.get("accessToken");
            String refreshToken = tokenMap.get("refreshToken");
            return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
        } catch (ExecutionException | InterruptedException e) {
            log.error("카카오 로그인 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "카카오 로그인 실패"));
        }
    }
}
