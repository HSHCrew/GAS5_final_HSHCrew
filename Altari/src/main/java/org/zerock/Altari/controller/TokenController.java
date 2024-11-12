package org.zerock.Altari.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.zerock.Altari.dto.UserDTO;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class TokenController {

    private final UserService userService; //
    private final JWTUtil jwtUtil;

    private Map<String, String> makeData(String username, String accessToken, String refreshToken) {
        return Map.of("username", username, "accessToken", accessToken, "refreshToken", refreshToken); //
    }

    private Map<String, String> makeNewToken(String username, String refreshToken) {
        Map<String, Object> claims = jwtUtil.validateToken(refreshToken);

        log.info("refresh token claims:" + claims);

        if (!username.equals(claims.get("username").toString())) { //
            throw new RuntimeException("Invalid Refresh Token Host");
        }

        UserDTO userDTO = userService.getByUsername(username); //

        Map<String, Object> newClaims = userDTO.getDataMap();

        String newAccessToken = jwtUtil.createToken(newClaims, 60);

        String newRefreshToken = jwtUtil.createToken(Map.of("username", username), 60 * 24 * 30); //

        return makeData(username, newAccessToken, newRefreshToken); //
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> makeToken(@RequestBody UserDTO userDTO) { //
        log.info("make token.....");

        UserDTO userDTOResult = userService.read(userDTO.getUsername(), userDTO.getPassword()); //

        log.info(userDTOResult);

        String username = userDTOResult.getUsername(); //

        Map<String, Object> dataMap = userDTOResult.getDataMap();

        String accessToken = jwtUtil.createToken(dataMap, 60);

        String refreshToken = jwtUtil.createToken(Map.of("username", username), 60 * 24 * 30); //

        log.info("accessToken: " + accessToken);
        log.info("refreshToken: " + refreshToken);

        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestHeader("Authorization") String accessTokenStr,
            @RequestParam("refreshToken") String refreshTokenStr,
            @RequestParam("username") String username) { //

        log.info("access token with Bearer........" + accessTokenStr);

        if (accessTokenStr == null || !accessTokenStr.startsWith("Bearer ")) {
            return handleException("No Access Token", 400);
        }

        if (refreshTokenStr == null) {
            return handleException("No Refresh Token", 400);
        }

        log.info("refreshToken........... " + refreshTokenStr);

        if (username == null) { //
            return handleException("No Username", 400);
        }

        String accessToken = accessTokenStr.substring(7);

        try {
            jwtUtil.validateToken(accessToken);

            Map<String, String> data = makeData(username, accessToken, refreshTokenStr); //

            return ResponseEntity.ok(data);

        } catch (io.jsonwebtoken.ExpiredJwtException expiredJwtException) {
            try {
                Map<String, String> newTokenMap = makeNewToken(username, refreshTokenStr); //
                return ResponseEntity.ok(newTokenMap);
            } catch (Exception e) {
                return handleException("REFRESH " + e.getMessage(), 400);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return handleException(e.getMessage(), 400);
        }
    }

    private ResponseEntity<Map<String, String>> handleException(String msg, int status) {
        return ResponseEntity.status(status).body(Map.of("msg", msg)); //
    }

}