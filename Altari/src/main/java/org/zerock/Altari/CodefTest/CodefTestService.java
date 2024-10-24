package org.zerock.Altari.CodefTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class CodefTestService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String API_URL = "https://development.codef.io/v1/kr/public/hw/hira-list/my-medicine";

    // 액세스 토큰을 클래스 변수로 선언
    private String accessToken = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXJ2aWNlX3R5cGUiOiIxIiwic2NvcGUiOlsicmVhZCJdLCJzZXJ2aWNlX25vIjoiMDAwMDA0NzYwMDAyIiwiZXhwIjoxNzMwMzYzMjg0LCJhdXRob3JpdGllcyI6WyJJTlNVUkFOQ0UiLCJQVUJMSUMiLCJCQU5LIiwiRVRDIiwiU1RPQ0siLCJDQVJEIl0sImp0aSI6IjAxMjdiZjI4LTRjZTktNDU2Yi05ODFiLWVkMDE1NTM5NDhjZSIsImNsaWVudF9pZCI6IjBlYTMxNjIwLTIwMTctNDQ2MS1iYzIyLTk2YzM3ZWU5OWFmMSJ9.ZaKTRyn3JJI98CZK82ZcsvKgoCV5rg0K6X4vnr_fzRKoWXHzuoxG1_-_lQpn9WQBx3ctbaksn1xN26g1L8HyQhPg4xBiEUNugw_Rn4ol8Cn4eZMS9pInHhD4QbSCFXr_97WzGPx9Gt9skjCew8Crrv9cHV6d8_mYtk5eaQnqG2aLdpjHGFI4-ir4u4h39R4_-Tk59cjjj-mbyKOJWKgghiBWYzDAw357JZiwzggdz5xIeQ46sWczUZq3v8DPhMfQJ-ClTaAI2btkdhYhHVxXhBcZUEleZPRS9X89qcNGwYLU5uEhc7BhS0aZUrbczx1OLg8bo1lhzug_W0JV8c8r9w";
    public String callApi(String organization,
                          String loginType,
                          String identity,
                          String loginTypeLevel,
                          String userName,
                          String phoneNo,
                          String authMethod,
                          String telecom,
                          String startDate,
                          String secureNoYN,
                          String reqChildYN) {
        try {

            // MedicineRequestDTO 객체 생성 및 데이터 설정
            MedicineRequestDTO requestDTO = MedicineRequestDTO.builder()
                    .organization(organization)
                    .loginType(loginType)
                    .identity(identity)
                    .loginTypeLevel(loginTypeLevel)
                    .userName(userName)
                    .phoneNo(phoneNo)
                    .authMethod(authMethod)
                    .telecom(telecom)
                    .startDate(startDate)
                    .secureNoYN(secureNoYN)
                    .reqChildYN(reqChildYN)
                    .build();

            // 요청 헤더에 Authorization 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            // HttpEntity를 사용하여 요청 본문과 헤더를 포함합니다.
            HttpEntity<MedicineRequestDTO> requestEntity = new HttpEntity<>(requestDTO, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, requestEntity, String.class);

            // 응답 본문을 URL 디코딩합니다.
            String responseBody = response.getBody();
            String decodedResponseBody = URLDecoder.decode(responseBody, StandardCharsets.UTF_8.name());

            return decodedResponseBody;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String callSecondApi(String organization,  String smsAuthNo, boolean is2Way,  String jti, int jobIndex, int threadIndex, long twoWayTimestamp ) {
        try {


            // 두 번째 호출을 위한 요청 데이터 설정
            SecondApiRequestDTO secondRequestDTO = SecondApiRequestDTO.builder()
                    .organization(organization)
                    .smsAuthNo(smsAuthNo)
                    .is2Way(is2Way)
                    .twoWayInfo(new TwoWayInfoDTO(jobIndex, threadIndex, jti, twoWayTimestamp))
                    .build();

            // 요청 헤더에 Authorization 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            // HttpEntity를 사용하여 요청 본문과 헤더를 포함합니다.
            HttpEntity<SecondApiRequestDTO> requestEntity = new HttpEntity<>(secondRequestDTO, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, requestEntity, String.class);

            // 두 번째 API 호출의 응답을 URL 디코딩합니다.
            String secondResponseBody = response.getBody();
            String decodedSecondResponseBody = URLDecoder.decode(secondResponseBody, StandardCharsets.UTF_8.name());

            return decodedSecondResponseBody;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
