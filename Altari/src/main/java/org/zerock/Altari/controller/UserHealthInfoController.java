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
@RequestMapping("/api/v1")
@Log4j2
@RequiredArgsConstructor
public class UserHealthInfoController {

    @Autowired
    private final UserHealthInfoService userHealthInfoService;

    @GetMapping("/get-userHealth/{username}")
    public ResponseEntity<UserHealthInfoDTO> getUserHealthInfo(@PathVariable String username) {
        UserEntity userEntity = new UserEntity(username);
        UserHealthInfoDTO userHealthInfoDTO = userHealthInfoService.getUserHealthInfo(userEntity);
        return ResponseEntity.ok(userHealthInfoDTO);
    }

    @PutMapping("/update-userHealth/{username}")
    public ResponseEntity<List<UserDiseaseEntity>> updateUserProfile(@PathVariable String username,
                                                           @Valid @RequestBody UserHealthInfoDTO userHealthInfoDTO) {
        UserEntity userEntity = new UserEntity(username);
        List<UserDiseaseEntity> result = userHealthInfoService.updateUserHealthInfo(userEntity, userHealthInfoDTO);

        return ResponseEntity.ok(result);
    }

// {
//     "disease_id":[4,5,6],
//     "past_disease_id":[3,5,7],
//     "family_disease_id":[3,4,7],
//     "allergy_medication_id":[9,4,12]
// }
}
