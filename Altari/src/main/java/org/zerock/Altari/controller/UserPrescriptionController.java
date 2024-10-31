package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.Altari.dto.UserHealthInfoDTO;
import org.zerock.Altari.dto.UserPrescriptionDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;
import org.zerock.Altari.service.UserPrescriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Log4j2
@RequiredArgsConstructor
public class UserPrescriptionController {

    private final UserPrescriptionService userPrescriptionService;

    @GetMapping("/get-userPrescription/{username}")
    public ResponseEntity<List<UserPrescriptionDTO>> get(@PathVariable String username) {
        UserEntity userEntity = new UserEntity(username);
        List<UserPrescriptionDTO> userPrescription = userPrescriptionService.getUserPrescription(userEntity);
        return ResponseEntity.ok(userPrescription);
    }


}
