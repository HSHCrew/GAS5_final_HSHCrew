package org.zerock.Altari.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.UserProfileRepository;
import org.zerock.Altari.repository.UserRepository;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
@Log4j2
public class JWTUtil {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret.key}") // application.properties에서 키 값 주입
    private String key;

    public JWTUtil(UserRepository userRepository, UserRepository userRepository1, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository1;
        this.userProfileRepository = userProfileRepository;
    }

    public String createToken(Map<String, Object> valueMap, int min) {

        SecretKey secretKey = null;

        try {
            secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return Jwts.builder().header().add("typ", "JWT").add("alg", "HS256").and().issuedAt(Date.from(ZonedDateTime.now().toInstant())).expiration((Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))).claims(valueMap).signWith(secretKey).compact();

    }

    public Map<String, Object> validateToken(String token) {

        SecretKey secretKey = null;

        try {
            secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

        log.info("claims: " + claims);

        return claims;

    }

    public static String getJwtFromHeader(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            // "Bearer " 접두사를 제거하고 JWT 토큰 반환
            return header.substring(BEARER_PREFIX.length());
        }

        return null; // 헤더가 없거나 형식이 올바르지 않으면 null 반환
    }

    public UserEntity getUsernameFromToken(String token) throws UnsupportedEncodingException {

        SecretKey secretKey = null;
        try {
            secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));

            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token.substring(7)).getBody();

            String username = claims.get("username", String.class);

            Optional<UserEntity> result = userRepository.findById(username);
            UserEntity userEntity = result.orElseThrow(UserExceptions.NOT_FOUND::get);

            return userEntity;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public UserProfileEntity getUserProfileFromToken(String token) throws UnsupportedEncodingException {

        SecretKey secretKey = null;
        try {
            secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));

            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token.substring(7)).getBody();

            String username = claims.get("username", String.class);

            Optional<UserEntity> result = userRepository.findById(username);
            UserEntity userEntity = result.orElseThrow(UserExceptions.NOT_FOUND::get);

            UserProfileEntity userProfile = userProfileRepository.findByUsername(userEntity);

            return userProfile;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
