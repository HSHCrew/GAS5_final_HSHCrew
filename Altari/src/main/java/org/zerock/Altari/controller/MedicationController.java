package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.MedicationCompletionDTO;
import org.zerock.Altari.dto.TimedMedicationDTO;
import org.zerock.Altari.dto.UserMedicationTimeDTO;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.EntityNotMatchedException;
import org.zerock.Altari.repository.MedicationRepository;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.MedicationAlarmService;
import org.zerock.Altari.service.MedicationService;
import org.zerock.Altari.service.UserMedicationTimeService;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationRepository medicationRepository;
    private final MedicationAlarmService medicationAlarmService;
    private final UserMedicationTimeService userMedicationTimeService;
    private final JWTUtil jWTUtil;
    private final MedicationService medicationService;

    @GetMapping("/drug/list")
    public List<MedicationEntity> getAllDrugs() {
        return medicationService.getAllDrugs();
    }

    @GetMapping("/drug-info/{medicationId}")
    public MedicationEntity getDrugInfo(@PathVariable Integer medicationId) {
        return medicationService.getDrugInfo(medicationId);
    }

    @GetMapping("/drug-TimedMedication/{PrescriptionId}")
    public TimedMedicationDTO getTimedMedication(@PathVariable Integer PrescriptionId) {
        return medicationService.getTimedMedicationList(PrescriptionId);
    }

    @PostMapping("/confirm/{username}")
    public ResponseEntity<String> confirmMedication(@PathVariable String username,
                                                    @RequestBody MedicationCompletionDTO medicationCompletionDTO,
                                                    @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jWTUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        try {
            medicationAlarmService.confirmMedication(user, medicationCompletionDTO); // 복용 확인 메서드 호출
            return ResponseEntity.ok("약 복용이 확인되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("복용 확인 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/medication/progress/{username}")
    public ResponseEntity<Map<String, Object>> getProgressByPrescription(@PathVariable String username,
                                                                         @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jWTUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }

        Map<String, Object> userProgress = medicationAlarmService.calculateProgressByPrescription(user);

        return ResponseEntity.ok(userProgress);
    }

    @PostMapping("/medication/onAlarm/{username}")
    public ResponseEntity<UserMedicationTimeDTO> setOnAlarm(@PathVariable String username,
                                                            @RequestBody UserMedicationTimeDTO userMedicationTimeDTO,
                                                            @RequestHeader("Authorization") String accessToken
    ) throws UnsupportedEncodingException {
        UserEntity userToken = jWTUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }

        UserMedicationTimeDTO updatedMedicationTime = userMedicationTimeService.updateMedicationAlarmStatus(user, userMedicationTimeDTO);
        return ResponseEntity.ok(updatedMedicationTime);
    }

    @GetMapping("/medication/getAlarm/{username}")
    public ResponseEntity<UserMedicationTimeDTO> getOnAlarm(@PathVariable String username,
                                                            @RequestHeader("Authorization") String accessToken
                                                               ) throws UnsupportedEncodingException {
        UserEntity userToken = jWTUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }
        UserMedicationTimeDTO MedicationTime = userMedicationTimeService.getMedicationTime(user);
        return ResponseEntity.ok(MedicationTime);
    }

    @GetMapping("/medication/getMedicationCompletion/{username}")
    public ResponseEntity<List<MedicationCompletionDTO>> getUserMedicationCompletion(@PathVariable String username,
                                                                                     @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity userToken = jWTUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // 3. userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }

        List<MedicationCompletionDTO> medicationCompletion = medicationAlarmService.getMedicationCompletion(user);

        return ResponseEntity.ok(medicationCompletion);
    }


}
