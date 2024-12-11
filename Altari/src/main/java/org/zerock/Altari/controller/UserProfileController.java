package org.zerock.Altari.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.RegisterDTO;
import org.zerock.Altari.dto.UserHealthInfoDTO;
import org.zerock.Altari.dto.UserProfileDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.exception.EntityNotMatchedException;
import org.zerock.Altari.repository.UserProfileRepository;
import org.zerock.Altari.repository.UserRepository;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.MedicationAlarmService;
import org.zerock.Altari.service.UserProfileService;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileRepository userProfileRepository;
    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    private final MedicationAlarmService medicationAlarmService;
    private final JWTUtil jwtUtil;

    //
    @GetMapping("/getInfo/userProfile/{username}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username, @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserProfileDTO userProfile = userProfileService.getUserProfile(user);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/updateInfo/userProfile/{username}")
    public ResponseEntity<UserProfileDTO> updateUserProfile(@PathVariable String username, @Valid @RequestBody UserProfileDTO userProfileDTO, @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserProfileDTO updatedProfile = userProfileService.updateUserProfile(user, userProfileDTO);
        return ResponseEntity.ok(updatedProfile);
    }


}
