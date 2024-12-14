package org.zerock.Altari.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.MedicineRequestDTO;
import org.zerock.Altari.dto.SecondApiRequestDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.repository.UserProfileRepository;
import org.zerock.Altari.repository.UserRepository;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.CodefTestService;
import org.zerock.Altari.service.MedicationAlarmService;
import org.zerock.Altari.service.UserService;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/altari")
public class CodefTestController {

    @Autowired
    private CodefTestService codefTestService;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private MedicationAlarmService medicationAlarmService;

    // 첫 번째 API 호출을 위한 엔드포인트
    @PostMapping("/prescriptions/enter-info")
    public ResponseEntity<String> callApi(@RequestBody MedicineRequestDTO requestDTO)throws Exception{
        // 전달된 DTO 데이터를 사용하여 첫 번째 API 호출
        CompletableFuture<String> responseFuture = codefTestService.callApi(

                requestDTO.getIdentity(),
                requestDTO.getUserName(),
                requestDTO.getPhoneNo()
        );

        String response = responseFuture.get();

        return ResponseEntity.ok(response);
    }

    // 두 번째 API 호출을 위한 엔드포인트
    @PostMapping("/prescriptions/verify-auth")
    public ResponseEntity<String> callSecondApi(@RequestBody SecondApiRequestDTO secondRequestDTO,
                                                @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {
        // 전달된 DTO 데이터를 사용하여 두 번째 API 호출
        UserEntity user = jwtUtil.getUserFromToken(accessToken);

        String response = codefTestService.callSecondApi(
                secondRequestDTO.is2Way(),
                secondRequestDTO.getTwoWayInfo().getJti(),
                secondRequestDTO.getTwoWayInfo().getJobIndex(),
                secondRequestDTO.getTwoWayInfo().getThreadIndex(),
                secondRequestDTO.getTwoWayInfo().getTwoWayTimestamp(),
                user
        );

        try {
            medicationAlarmService.userScheduleAlerts(user);
        } catch (Exception e) {
            // 예외 발생 시 로그를 남기고 계속 진행
            System.err.println("복약 알림 예약 중 오류 발생: " + e.getMessage());
            e.printStackTrace(); // 상세한 오류 메시지를 콘솔에 출력 (필요에 따라 생략 가능)
        }

        return ResponseEntity.ok(response);
    }
}

