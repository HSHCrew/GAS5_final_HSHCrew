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
import org.zerock.Altari.service.UserService;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/codef")
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

    // 첫 번째 API 호출을 위한 엔드포인트
    @PostMapping("/first")
    ///{
    ///     "organization": "0020",
    ///     "loginType": "5",
    ///     "loginTypeLevel": "1",
    ///     "identity": "001**********",
    ///     "userName": "전**",
    ///     "phoneNo": "010********",
    ///     "startDate": "",
    ///     "telecom": "",
    ///     "id": "",
    ///     "reqChildYN": "0"
    /// }
    public ResponseEntity<String> callApi(@RequestBody MedicineRequestDTO requestDTO) {
        // 전달된 DTO 데이터를 사용하여 첫 번째 API 호출
        String response = codefTestService.callApi(
                requestDTO.getOrganization(),
                requestDTO.getLoginType(),
                requestDTO.getIdentity(),
                requestDTO.getLoginTypeLevel(),
                requestDTO.getUserName(),
                requestDTO.getPhoneNo(),
                requestDTO.getTelecom(),
                requestDTO.getStartDate(),
                requestDTO.getId(),
                requestDTO.getReqChildYN()

        );
        return ResponseEntity.ok(response);
    }

    // 두 번째 API 호출을 위한 엔드포인트
    @PostMapping("/second")
    public ResponseEntity<String> callSecondApi(@RequestBody SecondApiRequestDTO secondRequestDTO,
                                                @RequestHeader("Authorization") String token) throws UnsupportedEncodingException {
        // 전달된 DTO 데이터를 사용하여 두 번째 API 호출
//
        UserProfileEntity userProfile = jwtUtil.extractUsername(token.substring(7)); // "Bearer " 이후의 토큰 문자열
//
//

        String response = codefTestService.callSecondApi(
                secondRequestDTO.getOrganization(),
                secondRequestDTO.getSimpleAuth(),
                secondRequestDTO.is2Way(),
                secondRequestDTO.getTwoWayInfo().getJti(),
                secondRequestDTO.getTwoWayInfo().getJobIndex(),
                secondRequestDTO.getTwoWayInfo().getThreadIndex(),
                secondRequestDTO.getTwoWayInfo().getTwoWayTimestamp(),
                userProfile
                );



        return ResponseEntity.ok(response);
    }
}
