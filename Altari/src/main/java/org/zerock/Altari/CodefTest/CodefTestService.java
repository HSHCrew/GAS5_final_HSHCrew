package org.zerock.Altari.CodefTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.List;
import java.util.Optional;

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

    @Transactional
    public String callApi(String identity,
                          String userName,
                          String phoneNo
    ) {
        try {

            // MedicineRequestDTO 객체 생성 및 데이터 설정
            MedicineRequestDTO requestDTO = MedicineRequestDTO.builder()
                    .organization("0020")
                    .loginType("5")
                    .identity(identity)
                    .loginTypeLevel("1")
                    .userName(userName)
                    .phoneNo(phoneNo)
                    .startDate("")
                    .telecom("")
                    .id("")
                    .reqChildYN("")
                    .build();

            // 요청 헤더에 Authorization 추가
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
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


    @Transactional
    public String callSecondApi(boolean is2Way, String jti, int jobIndex, int threadIndex, long twoWayTimestamp, UserProfileEntity userProfile) {
        try {
            // 두 번째 호출을 위한 요청 데이터 설정
            SecondApiRequestDTO secondRequestDTO = SecondApiRequestDTO.builder()
                    .organization("0020")
                    .simpleAuth("1")
                    .is2Way(is2Way)
                    .twoWayInfo(new TwoWayInfoDTO(jobIndex, threadIndex, jti, twoWayTimestamp))
                    .build();
            String accessToken = easyCodefToken.getAccessToken().trim();
            System.out.println("Access Token: " + accessToken);

            // 요청 헤더에 Authorization 추가
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

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
                String prescribeNo = data.get("resPrescribeNo").asText();

                // prescribe_no가 이미 존재하는지 확인
                Optional<UserPrescriptionEntity> existingPrescription = userPrescriptionRepository.findByPrescribeNo(prescribeNo);
                if (existingPrescription.isPresent()) {
                    continue;
                }

                // 새 처방전 저장
                String commBrandName = data.get("commBrandName").asText();
                String telNo = data.get("resTelNo").asText();
                String prescribeOrg = data.get("resPrescribeOrg").asText();
                String telNo1 = data.get("resTelNo1").asText();
                String manufactureDateStr = data.get("resManufactureDate").asText();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate manufactureDate = LocalDate.parse(manufactureDateStr, dateTimeFormatter);

                UserPrescriptionEntity userPrescription = new UserPrescriptionEntity();
                userPrescription.setComm_brand_name(commBrandName);
                userPrescription.setTel_no(telNo);
                userPrescription.setPrescribe_org(prescribeOrg);
                userPrescription.setPrescribeNo(prescribeNo);
                userPrescription.setTel_no1(telNo1);
                userPrescription.setManufacture_date(manufactureDate);
                userPrescription.setUserProfile(userProfile);
                userPrescription.setIsTaken(false);

                userPrescriptionRepository.save(userPrescription);

                JsonNode drugList = data.get("resDrugList");
                int totalDosingDays = 0;

                for (JsonNode drugData : drugList) {
                    Integer drugCode = Integer.parseInt(drugData.get("resDrugCode").asText());

                    MedicationEntity drugItemSeq = medicationRepository.findByMedicationId(drugCode);
                    if (drugItemSeq != null) {
                        PrescriptionDrugEntity prescriptionDrug = new PrescriptionDrugEntity();
                        prescriptionDrug.setPrescriptionId(userPrescription);
                        prescriptionDrug.setMedicationId(drugItemSeq);
                        prescriptionDrug.setOne_dose(drugData.get("resOneDose").asText());
                        prescriptionDrug.setDailyDosesNumber(Integer.parseInt(drugData.get("resDailyDosesNumber").asText()));
                        int drugTotalDosingDays = Integer.parseInt(drugData.get("resTotalDosingdays").asText());
                        prescriptionDrug.setTaken_dosing_days(0);
                        prescriptionDrug.setTotal_dosing_days(drugTotalDosingDays);
                        prescriptionDrug.setMedication_direction(drugData.get("resMedicationDirection").asText());
                        prescriptionDrug.setTotal_dosage(drugTotalDosingDays * prescriptionDrug.getDailyDosesNumber());
                        prescriptionDrug.setTaken_dosage(0);
                        prescriptionDrug.setTodayTakenCount(0);

                        totalDosingDays = Math.max(totalDosingDays, drugTotalDosingDays);
                        prescriptionDrugRepository.save(prescriptionDrug);
                    }
                }

                // 총 복용 일수를 기준으로 처방전의 is_taken 값을 설정
                LocalDate endDate = manufactureDate.plusDays(totalDosingDays);
                if (endDate.isBefore(LocalDate.now())) {
                    userPrescription.setIsTaken(true); // 복용 완료

                    // 처방전의 약물 리스트에 대해 taken_dosage를 total_dosage와 같게 설정
                    List<PrescriptionDrugEntity> prescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(userPrescription);
                    for (PrescriptionDrugEntity prescriptionDrug : prescriptionDrugs) {
                        prescriptionDrug.setTaken_dosing_days(prescriptionDrug.getTotal_dosing_days());
                        prescriptionDrug.setTaken_dosage(prescriptionDrug.getTotal_dosage());
                        prescriptionDrugRepository.save(prescriptionDrug);
                    }
                } else {
                    userPrescription.setIsTaken(false); // 복용 미완료
                }

                // 변경된 is_taken 값 업데이트
                userPrescriptionRepository.save(userPrescription);
            }

            return decodedSecondResponseBody;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}




