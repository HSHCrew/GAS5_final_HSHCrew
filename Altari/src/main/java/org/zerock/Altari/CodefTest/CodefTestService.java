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
    private String accessToken = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXJ2aWNlX3R5cGUiOiIxIiwic2NvcGUiOlsicmVhZCJdLCJzZXJ2aWNlX25vIjoiMDAwMDA0NzYwMDAyIiwiZXhwIjoxNzMwMzY2OTM2LCJhdXRob3JpdGllcyI6WyJJTlNVUkFOQ0UiLCJQVUJMSUMiLCJCQU5LIiwiRVRDIiwiU1RPQ0siLCJDQVJEIl0sImp0aSI6ImIyNjg0MDVhLWQzNjYtNGE1Yi04ODM3LWQxZTliOWU0ZmRkOCIsImNsaWVudF9pZCI6IjBlYTMxNjIwLTIwMTctNDQ2MS1iYzIyLTk2YzM3ZWU5OWFmMSJ9.NYdmH57Y2uwoZhXcofZPtE5qYVjvWQFjDKIhDSQSmuLa5Fd2yCAhqp0DPavaKh5KUqrogpHzPpQyg8hF_UzTH6tnug3EzN0hnaeGPl3c6IrZlzDkt9bM7ZhY5amN__cVXEfvS3ZCXdyyJmmmmAqY-Vc3NGsbKsgGhn4mLXE0UgJSsBNHhitBaWi7BdUA7gpp-OBj96HVWW1oPT2neSc8l-TLxF5gfeq2JTlqtsUyQUNIdnvGubMfvNJIVLuBkEplBb3CbPrETAx5O-SjKEMS1oIZ13SKaK_9VS72HvfxvWn4Awb8bDVzpKOx9d407I1QSpF2wvTZuTEz66MRGBW3IA";
    public String callApi(String organization,
                          String loginType,
                          String identity,
                          String loginTypeLevel,
                          String userName,
                          String phoneNo,
                          String telecom,
                          String startDate,
                          String id,
                          String reqChildYN,
                          String detailYN) {
        try {

            // MedicineRequestDTO 객체 생성 및 데이터 설정
            MedicineRequestDTO requestDTO = MedicineRequestDTO.builder()
                    .organization(organization)
                    .loginType(loginType)
                    .identity(identity)
                    .loginTypeLevel(loginTypeLevel)
                    .userName(userName)
                    .phoneNo(phoneNo)
                    .startDate(startDate)
                    .telecom(telecom)
                    .id(id)
                    .reqChildYN(reqChildYN)
                    .detailYN(detailYN)
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

    public String callSecondApi(String organization,  String simpleAuth, boolean is2Way,  String jti, int jobIndex, int threadIndex, long twoWayTimestamp ) {
        try {


            // 두 번째 호출을 위한 요청 데이터 설정
            SecondApiRequestDTO secondRequestDTO = SecondApiRequestDTO.builder()
                    .organization(organization)
                    .simpleAuth(simpleAuth)
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
