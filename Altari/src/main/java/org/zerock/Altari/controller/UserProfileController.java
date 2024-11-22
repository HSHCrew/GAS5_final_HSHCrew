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
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username,

                                                         @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jwtUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        UserProfileDTO userProfile = userProfileService.getUserProfile(user);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/updateInfo/userProfile/{username}")
    public ResponseEntity<UserProfileDTO> updateUserProfile(@PathVariable String username,
                                                               @Valid @RequestBody UserProfileDTO userProfileDTO,
                                                            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jwtUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        UserProfileDTO updatedProfile = userProfileService.updateUserProfile(user, userProfileDTO);
        medicationAlarmService.userScheduleAlerts(user);

        return ResponseEntity.ok(updatedProfile);
    }
    // {
//     "username": "test2",                // 사용자가 가입할 아이디
//     "password": "1111",          // 사용자 비밀번호
//     "role": "USER",                        // 사용자 역할 (예: USER, ADMIN)
//     "full_name": "홍길동",                  // 사용자 이름
//     "date_of_birth": "1990-01-01",        // 생년월일 (YYYY-MM-DD 형식)
//     "phone_number": "010-1121-1278"    // 전화번호
// }


}
