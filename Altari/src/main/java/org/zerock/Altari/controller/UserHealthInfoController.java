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

    // 1. 사용자 건강 정보 조회
    @GetMapping("/getInfo/userHealth/{username}")
    public ResponseEntity<UserHealthInfoDTO> getUserHealthInfo(@PathVariable String username,
                                                               @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        // JWT에서 사용자 정보 추출
        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserHealthInfoDTO userHealthInfoDTO = userHealthInfoService.getUserHealthInfo(user);

        return ResponseEntity.ok(userHealthInfoDTO);
    }

    // 2. 현재 질병 추가
    @PutMapping("/updateInfo/userDisease/{diseaseId}")
    public ResponseEntity<String> addCurrentDisease(@PathVariable Integer diseaseId,
                                                    @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userHealthInfoService.addCurrentDisease(user, diseaseId);

        return ResponseEntity.ok("현재 질병이 성공적으로 추가되었습니다.");
    }

    // 3. 과거 질병 추가
    @PutMapping("/updateInfo/userPastDisease/{diseaseId}")
    public ResponseEntity<String> addPastDisease(@PathVariable Integer diseaseId,
                                                 @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userHealthInfoService.addPastDisease(user, diseaseId);

        return ResponseEntity.ok("과거 질병이 성공적으로 추가되었습니다.");
    }

    // 4. 가족력 추가
    @PutMapping("/updateInfo/userFamilyDisease/{diseaseId}")
    public ResponseEntity<String> addFamilyHistory(@PathVariable Integer diseaseId,
                                                   @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userHealthInfoService.addFamilyHistory(user, diseaseId);

        return ResponseEntity.ok("가족력이 성공적으로 추가되었습니다.");
    }

    // 5. 알레르기 추가
    @PutMapping("/updateInfo/userAllergy/{medicationId}")
    public ResponseEntity<String> addAllergy(@PathVariable Integer medicationId,
                                             @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userHealthInfoService.addAllergy(user, medicationId);

        return ResponseEntity.ok("알레르기 약물이 성공적으로 추가되었습니다.");
    }

    // 6. 현재 질병 삭제
    @DeleteMapping("/deleteInfo/userDisease/{diseaseId}")
    public ResponseEntity<String> removeCurrentDisease(@PathVariable Integer diseaseId,
                                                       @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userHealthInfoService.removeCurrentDisease(user, diseaseId);

        return ResponseEntity.ok("현재 질병이 성공적으로 삭제되었습니다.");
    }

    // 7. 과거 질병 삭제
    @DeleteMapping("/deleteInfo/userPastDisease/{diseaseId}")
    public ResponseEntity<String> removePastDisease(@PathVariable Integer diseaseId,
                                                    @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userHealthInfoService.removePastDisease(user, diseaseId);

        return ResponseEntity.ok("과거 질병이 성공적으로 삭제되었습니다.");
    }

    // 8. 가족력 삭제
    @DeleteMapping("/deleteInfo/userFamilyDisease/{diseaseId}")
    public ResponseEntity<String> removeFamilyHistory(@PathVariable Integer diseaseId,
                                                      @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userHealthInfoService.removeFamilyHistory(user, diseaseId);

        return ResponseEntity.ok("가족력이 성공적으로 삭제되었습니다.");
    }

    // 9. 알레르기 삭제
    @DeleteMapping("/deleteInfo/userAllergy/{medicationId}")
    public ResponseEntity<String> removeAllergy(@PathVariable Integer medicationId,
                                                @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userHealthInfoService.removeAllergy(user, medicationId);

        return ResponseEntity.ok("알레르기 약물이 성공적으로 삭제되었습니다.");
    }
}

