package org.zerock.Altari.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.UserHealthInfoDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.UserHealthInfoService;

import java.io.UnsupportedEncodingException;

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

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserHealthInfoDTO userHealthInfoDTO = userHealthInfoService.getUserHealthInfo(user);
        return ResponseEntity.ok(userHealthInfoDTO);
    }

    @PutMapping("/updateInfo/userDisease/{username}")
    public ResponseEntity<String> updateUserDisease(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO,
                                                    @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        String result = userHealthInfoService.updateUserDisease(user, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateInfo/userPastDisease/{username}")
    public ResponseEntity<String> updateUserPastDisease(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO,
                                                        @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        String result = userHealthInfoService.updateUserPastDisease(user, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateInfo/userFamilyDisease/{username}")
    public ResponseEntity<String> updateUserFamilyDisease(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO,
                                                          @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        String result = userHealthInfoService.updateUserFamilyDisease(user, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateInfo/userAllergy/{username}")
    public ResponseEntity<String> updateUserAllergy(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO,
                                                    @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        String result = userHealthInfoService.updateUserAllergy(user, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

}

