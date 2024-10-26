package org.zerock.Altari.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.Altari.dto.RegisterDTO;
import org.zerock.Altari.dto.UserProfileDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Log4j2
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserEntity> signUp(@Valid @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.join(registerDTO));

    }
    @PostMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        boolean isDuplicate = userService.isUsernameDuplicate(username);
        return ResponseEntity.ok(isDuplicate); //중복이면 true, 중복이 아니면 false
    }
//    @PostMapping("/update/profile")
//    public ResponseEntity<UserProfileEntity> updateProfile(@RequestBody UserProfileDTO userProfileDTO) {
//        UserProfileEntity updatedUserProfile = userService.updateUserProfile(userProfileDTO);
//        return ResponseEntity.ok(updatedUserProfile);
//
//    }
}

