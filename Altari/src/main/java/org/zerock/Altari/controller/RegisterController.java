package org.zerock.Altari.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.zerock.Altari.dto.RegisterDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.RegisterService;

import java.io.UnsupportedEncodingException;
import java.util.Map;

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

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        try {
            registerService.deleteUser(user);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴에 실패하였습니다.");
        }
    }


}

