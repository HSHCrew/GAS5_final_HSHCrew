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
import org.zerock.Altari.service.UserHealthInfoService;

import java.util.List;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class UserHealthInfoController {

    @Autowired
    private final UserHealthInfoService userHealthInfoService;

    @GetMapping("/getInfo/userHealth/{username}")
    public ResponseEntity<UserHealthInfoDTO> getUserHealthInfo(@PathVariable String username) {
        UserEntity userEntity = new UserEntity(username);
        UserHealthInfoDTO userHealthInfoDTO = userHealthInfoService.getUserHealthInfo(userEntity);
        return ResponseEntity.ok(userHealthInfoDTO);
    }

    @PutMapping("/updateInfo/userDisease/{username}")
    public ResponseEntity<String> updateUserDisease(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO) {
        UserEntity userEntity = new UserEntity(username);
        String result = userHealthInfoService.updateUserDisease(userEntity, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateInfo/userPastDisease/{username}")
    public ResponseEntity<String> updateUserPastDisease(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO) {
        UserEntity userEntity = new UserEntity(username);
        String result = userHealthInfoService.updateUserPastDisease(userEntity, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateInfo/userFamilyDisease/{username}")
    public ResponseEntity<String> updateUserFamilyDisease(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO) {
        UserEntity userEntity = new UserEntity(username);
        String result = userHealthInfoService.updateUserFamilyDisease(userEntity, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateInfo/userAllergy/{username}")
    public ResponseEntity<String> updateUserAllergy(@PathVariable String username,
                                                    @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO) {
        UserEntity userEntity = new UserEntity(username);
        String result = userHealthInfoService.updateUserAllergy(userEntity, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

}

