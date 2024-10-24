package org.zerock.Altari.CodefTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/codef")
public class CodefTestController {

    @Autowired
    private CodefTestService codefTestService;

    // 첫 번째 API 호출을 위한 엔드포인트
    @PostMapping("/first")
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
                requestDTO.getReqChildYN(),
                requestDTO.getDetailYN()

        );
        return ResponseEntity.ok(response);
    }

    // 두 번째 API 호출을 위한 엔드포인트
    @PostMapping("/second")
    public ResponseEntity<String> callSecondApi(@RequestBody SecondApiRequestDTO secondRequestDTO) {
        // 전달된 DTO 데이터를 사용하여 두 번째 API 호출
        String response = codefTestService.callSecondApi(
                secondRequestDTO.getOrganization(),
                secondRequestDTO.getSimpleAuth(),
                secondRequestDTO.is2Way(),
                secondRequestDTO.getTwoWayInfo().getJti(),
                secondRequestDTO.getTwoWayInfo().getJobIndex(),
                secondRequestDTO.getTwoWayInfo().getThreadIndex(),
                secondRequestDTO.getTwoWayInfo().getTwoWayTimestamp()
        );
        return ResponseEntity.ok(response);
    }
}
