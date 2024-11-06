package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.EntityNotFoundException;
import org.zerock.Altari.repository.MedicationRepository;
import org.zerock.Altari.service.MedicationAlarmService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Log4j2
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationRepository medicationRepository;
    private final MedicationAlarmService medicationAlarmService;

    @GetMapping("/drugs")
    public List<MedicationEntity> getAllDrugs() {
        List<MedicationEntity> drugs = medicationRepository.findAll();
        if (drugs.isEmpty()) {
            throw new EntityNotFoundException("No medications found in the database");
        }
        return drugs;
    }

    @PostMapping("/confirm/{username}")
    public ResponseEntity<String> confirmMedication(@PathVariable String username) {

        UserEntity user = new UserEntity(username);
        try {
            medicationAlarmService.confirmMedication(user); // 복용 확인 메서드 호출
            return ResponseEntity.ok("약 복용이 확인되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("복용 확인 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/medication-progress/{username}")
    public ResponseEntity<Map<String, Object>> getProgressByPrescription(@PathVariable String username) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);

        Map<String, Object> userProgress = medicationAlarmService.calculateProgressByPrescription(userEntity);

        return ResponseEntity.ok(userProgress);
    }
}
