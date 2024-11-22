package org.zerock.Altari.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.UserHealthInfoDTO;
import org.zerock.Altari.dto.UserProfileDTO;
import org.zerock.Altari.entity.UserDiseaseEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.exception.EntityNotMatchedException;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.UserHealthInfoService;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class UserHealthInfoController {

    @Autowired
    private final UserHealthInfoService userHealthInfoService;
    private final JWTUtil jwtUtil;

    @GetMapping("/getInfo/userHealth/{username}")
    public ResponseEntity<UserHealthInfoDTO> getUserHealthInfo(@PathVariable String username,
                                                               @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jwtUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        UserHealthInfoDTO userHealthInfoDTO = userHealthInfoService.getUserHealthInfo(user);
        return ResponseEntity.ok(userHealthInfoDTO);
    }

    @PutMapping("/updateInfo/userDisease/{username}")
    public ResponseEntity<String> updateUserDisease(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO,
                                                    @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jwtUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        String result = userHealthInfoService.updateUserDisease(user, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateInfo/userPastDisease/{username}")
    public ResponseEntity<String> updateUserPastDisease(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO,
                                                        @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jwtUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        String result = userHealthInfoService.updateUserPastDisease(user, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateInfo/userFamilyDisease/{username}")
    public ResponseEntity<String> updateUserFamilyDisease(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO,
                                                          @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jwtUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        String result = userHealthInfoService.updateUserFamilyDisease(user, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateInfo/userAllergy/{username}")
    public ResponseEntity<String> updateUserAllergy(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO,
                                                    @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jwtUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        String result = userHealthInfoService.updateUserAllergy(user, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

}

