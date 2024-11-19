package org.zerock.Altari.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.zerock.Altari.dto.RegisterDTO;
import org.zerock.Altari.dto.UserProfileDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.exception.EntityNotMatchedException;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.RegisterService;
import org.zerock.Altari.service.UserService;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;
    private final JWTUtil jwtUtil;


    @PostMapping("/register")
    public ResponseEntity<UserEntity> signUp(@Valid @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.join(registerDTO));

    }
    @PostMapping("/check/username")
    public ResponseEntity<Boolean> checkUsername(@RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        boolean isDuplicate = registerService.isUsernameDuplicate(username);
        return ResponseEntity.ok(isDuplicate); //중복이면 true, 중복이 아니면 false
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username,
                                             @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jwtUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        try {
            registerService.deleteUser(username);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴에 실패하였습니다.");
        }
    }


}

