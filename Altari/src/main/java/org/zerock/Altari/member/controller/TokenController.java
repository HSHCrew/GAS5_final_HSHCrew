package org.zerock.Altari.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.zerock.Altari.member.dto.MemberDTO;
import org.zerock.Altari.member.security.util.JWTUtil;
import org.zerock.Altari.member.service.MemberService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/token")
@Log4j2
@RequiredArgsConstructor
public class TokenController {

    private final MemberService memberService;
//
    private final JWTUtil jwtUtil;

    private Map<String, String> makeData(String mid, String accessToken, String refreshToken) {
        return Map.of("mid",mid, "accessToken",accessToken, "refreshToken",refreshToken);
    }

    private Map<String, String> makeNewToken(String mid, String refreshToken) {

        Map<String, Object> claims = jwtUtil.validateToken(refreshToken);

        log.info("refresh token claims:" + claims);

        if (!mid.equals(claims.get("mid").toString())) {
            throw new RuntimeException("Invalid Refresh Token Host");
        }

        MemberDTO memberDTO = memberService.getByMid(mid);

        Map<String, Object> newClaims = memberDTO.getDataMap();

        String newAccessToken = jwtUtil.createToken(newClaims, 10);

        String newRefreshToken = jwtUtil.createToken(Map.of("mid", mid), 60 * 24 * 7);

        return makeData(mid, newAccessToken, newRefreshToken);
    }

    @PostMapping("/make")
    public ResponseEntity<Map<String, String>> makeToken(@RequestBody MemberDTO memberDTO) {

        log.info("make token.....");

        MemberDTO memberDTOResult = memberService.read(memberDTO.getMid(), memberDTO.getMpw());

        log.info(memberDTOResult);

        String mid = memberDTOResult.getMid();

        Map<String, Object> dataMap = memberDTOResult.getDataMap();

        String accessToken = jwtUtil.createToken(dataMap, 10);

        String refreshToken = jwtUtil.createToken(Map.of("mid", mid), 60 * 24 * 7);

        log.info("accessToken: " + accessToken);
        log.info("refreshToken: " + refreshToken);
//
        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));

    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestHeader("Authorization") String accessTokenStr, // HTTP 요청의 헤더에서 Authorization 값을 가져와 accessTokenStr에 저장
            @RequestParam("refreshToken") String refreshTokenStr, // URL 쿼리 파라미터에서 refresh 토큰 값을 가져와 변수에 할당
            @RequestParam("mid") String mid)

    // mid 값을 가져와 변수에 할당
    { // 기존의 토큰 값들을 가져옴

        log.info("access token with Bearer........" + accessTokenStr);

        if(accessTokenStr == null || !accessTokenStr.startsWith("Bearer ")){
            return handleException("No Access Token", 400);
        }

        if(refreshTokenStr == null) {
            return handleException("No Refresh Token", 400);
        }

        log.info("refreshToken........... " + refreshTokenStr);

        if (mid == null) {
            return handleException("No MID", 400);
        }

        //Access Token 만료확인
        String accessToken = accessTokenStr.substring(7);

        try {
            jwtUtil.validateToken(accessToken);

            Map<String, String> data = makeData(mid, accessToken, refreshTokenStr);

            return ResponseEntity.ok(data);

        }catch(io.jsonwebtoken.ExpiredJwtException expiredJwtException) {
            try{
                Map<String, String> newTokenMap = makeNewToken(mid, refreshTokenStr);
                return ResponseEntity.ok(newTokenMap);
            }catch(Exception e) {
                return handleException("REFRESH " + e.getMessage(), 400);
            }


        } catch (Exception e) {
            e.printStackTrace();
            return handleException(e.getMessage(), 400);
        }

    }

    private ResponseEntity<Map<String, String>> handleException(String mag, int status) {

        return ResponseEntity.status(status).body(Map.of("mag", mag));
    }

}
