package org.zerock.Altari.CodefTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.UserDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.repository.UserProfileRepository;
import org.zerock.Altari.repository.UserRepository;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.MedicationAlarmService;
import org.zerock.Altari.service.UserService;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/altari")
public class CodefTestController {

    @Autowired
    private CodefTestService codefTestService;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MedicationAlarmService medicationAlarmService;

    // 첫 번째 API 호출을 위한 엔드포인트
    @PostMapping("/prescriptions/enter-info")
    /// {
    ///     "identity": "001**********",
    ///     "userName": "전**",
    ///     "phoneNo": "010********",
    /// }
    public ResponseEntity<String> callApi(@RequestBody MedicineRequestDTO requestDTO) {
        // 전달된 DTO 데이터를 사용하여 첫 번째 API 호출
        String response = codefTestService.callApi(

                requestDTO.getIdentity(),
                requestDTO.getUserName(),
                requestDTO.getPhoneNo()
        );
        return ResponseEntity.ok(response);
    }

    // 두 번째 API 호출을 위한 엔드포인트
    @PostMapping("/prescriptions/verify-auth")
///    {
///        "is2Way": true,
///     "twoWayInfo": {
///         "jobIndex": 0,
///         "threadIndex": 0,
///         "jti": "672830f1ec82718833062d87",
///         "twoWayTimestamp": 1730687221057
///     }
///
/// }
    public ResponseEntity<String> callSecondApi(@RequestBody SecondApiRequestDTO secondRequestDTO,
                                                @RequestHeader("Authorization") String token) throws UnsupportedEncodingException {
        // 전달된 DTO 데이터를 사용하여 두 번째 API 호출
        UserProfileEntity userProfile = jwtUtil.getUserProfileFromToken(token); // "Bearer " 이후의 토큰 문자열
        UserEntity user = jwtUtil.getUsernameFromToken(token);

        String response = codefTestService.callSecondApi(
                secondRequestDTO.is2Way(),
                secondRequestDTO.getTwoWayInfo().getJti(),
                secondRequestDTO.getTwoWayInfo().getJobIndex(),
                secondRequestDTO.getTwoWayInfo().getThreadIndex(),
                secondRequestDTO.getTwoWayInfo().getTwoWayTimestamp(),
                userProfile
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

