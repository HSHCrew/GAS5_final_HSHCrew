package org.zerock.Altari.CodefTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zerock.Altari.Codef.EasyCodefToken;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.repository.MedicationRepository;
import org.zerock.Altari.repository.PrescriptionDrugRepository;
import org.zerock.Altari.repository.UserPrescriptionRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.JsonNode;
import org.zerock.Altari.repository.UserProfileRepository;

@Service
public class CodefTestService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserPrescriptionRepository userPrescriptionRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private EasyCodefToken easyCodefToken;

    private static final String API_URL = "https://development.codef.io/v1/kr/public/hw/hira-list/my-medicine";
    @Autowired
    private MedicationRepository medicationRepository;
    @Autowired
    private PrescriptionDrugRepository prescriptionDrugRepository;


    public String callApi(String organization,
                          String loginType,
                          String identity,
                          String loginTypeLevel,
                          String userName,
                          String phoneNo,
                          String telecom,
                          String startDate,
                          String id,
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
                    .startDate(startDate)
                    .telecom(telecom)
                    .id(id)
                    .reqChildYN(reqChildYN)
                    .build();

            // 요청 헤더에 Authorization 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + easyCodefToken.getAccessToken());

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


    public String callSecondApi(String organization, String simpleAuth, boolean is2Way, String jti, int jobIndex, int threadIndex, long twoWayTimestamp, UserProfileEntity userProfile) {
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
            headers.set("Authorization", "Bearer " + easyCodefToken.getAccessToken());

            // HttpEntity를 사용하여 요청 본문과 헤더를 포함합니다.
            HttpEntity<SecondApiRequestDTO> requestEntity = new HttpEntity<>(secondRequestDTO, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, requestEntity, String.class);

            // 두 번째 API 호출의 응답을 URL 디코딩합니다.
            String secondResponseBody = response.getBody();
            String decodedSecondResponseBody = URLDecoder.decode(secondResponseBody, StandardCharsets.UTF_8.name());

            // 응답 데이터 파싱
            JsonNode jsonResponse = objectMapper.readTree(decodedSecondResponseBody);
            JsonNode dataList = jsonResponse.get("data");

            // 각 처방전을 반복 처리
            for (JsonNode data : dataList) {
                String commBrandName = data.get("commBrandName").asText();
                String telNo = data.get("resTelNo").asText();
                String prescribeOrg = data.get("resPrescribeOrg").asText();
                String prescribeNo = data.get("resPrescribeNo").asText();
                String telNo1 = data.get("resTelNo1").asText();
                String manufactureDateStr = data.get("resManufactureDate").asText();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate manufactureDate = LocalDate.parse(manufactureDateStr, dateTimeFormatter);

                // UserPrescription 객체 생성 및 저장
                UserPrescriptionEntity userPrescription = new UserPrescriptionEntity();
                userPrescription.setComm_brand_name(commBrandName);
                userPrescription.setTel_no(telNo);
                userPrescription.setPrescribe_org(prescribeOrg);
                userPrescription.setPrescribe_no(prescribeNo);
                userPrescription.setTel_no1(telNo1);
                userPrescription.setManufacture_date(manufactureDate);
                userPrescription.setUserProfile(userProfile);
                userPrescriptionRepository.save(userPrescription);

                // 약물 리스트를 Prescription_Drug 테이블에 저장
                JsonNode drugList = data.get("resDrugList");
                for (JsonNode drugData : drugList) {
                    Integer drugCode = Integer.parseInt(drugData.get("resDrugCode").asText());

                    // 기존 약물 정보를 조회
                    MedicationEntity drugItemSeq = medicationRepository.findByMedicationId(drugCode);
                    if (drugItemSeq != null) {
                        // Prescription_Drug 객체 생성 및 저장
                        PrescriptionDrugEntity prescriptionDrug = new PrescriptionDrugEntity();
                        prescriptionDrug.setPrescriptionId(userPrescription);
                        prescriptionDrug.setMedicationId(drugItemSeq);
                        prescriptionDrug.setOne_dose(drugData.get("resOneDose").asText());
                        prescriptionDrug.setDailyDosesNumber(drugData.get("resDailyDosesNumber").asInt());
                        prescriptionDrug.setTotal_dosing_days(drugData.get("resTotalDosingdays").asInt());
                        prescriptionDrug.setMedication_direction(drugData.get("resMedicationDirection").asText());
                        int dailyDosesNumber = drugData.get("resDailyDosesNumber").asInt();
                        int totalDosingDays = drugData.get("resTotalDosingdays").asInt();
                        prescriptionDrug.setTotal_dosage(dailyDosesNumber * totalDosingDays);
                        prescriptionDrug.setTaken_dosage(0);
                        prescriptionDrugRepository.save(prescriptionDrug);
                    }
                }
            }

            return decodedSecondResponseBody;

        } catch (Exception e) {
            System.out.println("is2Way : " + is2Way);
            e.printStackTrace();
            return null;
        }
    }


}



