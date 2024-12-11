package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.UserHealthInfoDTO;
import org.zerock.Altari.dto.UserPrescriptionDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;
import org.zerock.Altari.exception.EntityNotMatchedException;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.UserPrescriontionService;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class UserPrescriptionController {


    private final UserPrescriontionService userPrescriptionService;
    private final JWTUtil jwtUtil;

    @GetMapping("/getInfo/userPrescription/{username}")
    public ResponseEntity<List<UserPrescriptionDTO>> getUserPrescription(@PathVariable String username, @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        List<UserPrescriptionDTO> userPrescription = userPrescriptionService.getUserPrescription(user);
        return ResponseEntity.ok(userPrescription);
    }

    @GetMapping("/getInfo/Prescription/{userPrescriptionId}")
    public ResponseEntity<UserPrescriptionDTO> getPrescription(@PathVariable Integer userPrescriptionId, @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserPrescriptionDTO userPrescription = userPrescriptionService.getPrescription(user, userPrescriptionId);
        return ResponseEntity.ok(userPrescription);
    }

}
