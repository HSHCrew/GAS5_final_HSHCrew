package org.zerock.Altari.CodefTest;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.zerock.Altari.repository.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

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
    private UserMedicationRepository prescriptionDrugRepository;
    @Autowired
    private MedicationCompletionRepository medicationCompletionRepository;

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

            // data가 리스트가 아닌 경우 처리
            if (!dataList.isArray()) {
                dataList = objectMapper.createArrayNode().add(dataList); // data가 단일 객체일 경우 배열로 변환
            }

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
                userPrescription.setCommBrandName(commBrandName);
                userPrescription.setTelNo(telNo);
                userPrescription.setPrescribeOrg(prescribeOrg);
                userPrescription.setPrescribeNo(prescribeNo);
                userPrescription.setTelNo2(telNo1);
                userPrescription.setManufactureDate(manufactureDate);
                userPrescription.setUserProfile(userProfile);
                userPrescription.setIsTaken(false);
                userPrescription.setOnAlarm(true);

                userPrescriptionRepository.save(userPrescription);

                MedicationCompletionEntity medicationCompletion = new MedicationCompletionEntity();
                medicationCompletion.setCreatedAt(LocalDate.now());
                medicationCompletion.setMorningTaken(false);
                medicationCompletion.setLunchTaken(false);
                medicationCompletion.setDinnerTaken(false);
                medicationCompletion.setNightTaken(false);
                medicationCompletion.setUserProfile(userProfile);

                medicationCompletionRepository.save(medicationCompletion);

                JsonNode drugList = data.get("resDrugList");

                // resDrugList가 배열이 아닐 경우 처리
                if (!drugList.isArray()) {
                    drugList = objectMapper.createArrayNode().add(drugList); // resDrugList가 단일 객체일 경우 배열로 변환
                }

                int totalDosingDays = 0;

                for (JsonNode drugData : drugList) {
                    String drugCodeStr = drugData.get("resDrugName").asText();  // drugCode는 String으로 받기
                    MedicationEntity drugItemSeq = medicationRepository.findByMedicationName(drugCodeStr);  // String으로 비교

                    if (drugItemSeq != null) {
                        UserMedicationEntity prescriptionDrug = new UserMedicationEntity();
                        prescriptionDrug.setPrescriptionId(userPrescription);
                        prescriptionDrug.setMedication(drugItemSeq);
                        prescriptionDrug.setUserProfile(userProfile);
                        prescriptionDrug.setOneDose(drugData.get("resOneDose").asText());
                        prescriptionDrug.setDailyDosesNumber(Integer.parseInt(drugData.get("resDailyDosesNumber").asText())); // dailyDosesNumber는 여전히 int로 받아야 하므로 Integer로 파싱
                        String drugTotalDosingDaysStr = drugData.get("resTotalDosingdays").asText();
                        prescriptionDrug.setTotalDosingDays(0);
                        prescriptionDrug.setTotalDosingDays(Integer.parseInt(drugTotalDosingDaysStr)); // totalDosingDays도 여전히 int로 받음
                        prescriptionDrug.setMedicationDirection(drugData.get("resMedicationDirection").asText());
                        prescriptionDrug.setTotalDosage(prescriptionDrug.getTotalDosingDays() * prescriptionDrug.getDailyDosesNumber());
                        prescriptionDrug.setTakenDosage(0);
                        prescriptionDrug.setTodayTakenCount(0);

                        totalDosingDays = Math.max(totalDosingDays, prescriptionDrug.getTotalDosingDays());
                        prescriptionDrugRepository.save(prescriptionDrug);

                        // 만약 medication_image가 없으면 resDrugImageLink에서 가져온 값을 삽입
                        if (drugItemSeq.getItemImage() == null || drugItemSeq.getItemImage().isEmpty()) {
                            String drugImageLink = drugData.get("resDrugImageLink").asText();
                            drugItemSeq.setItemImage(drugImageLink);
                            medicationRepository.save(drugItemSeq); // medication 테이블에 이미지 링크 저장
                        }
                    }
                }

                // 처방전의 최대 total_dosing_days 값을 userPrescription에 설정
                userPrescription.setTotalDosingDay(totalDosingDays);
                userPrescriptionRepository.save(userPrescription);

                // 총 복용 일수를 기준으로 처방전의 is_taken 값을 설정
                LocalDate endDate = manufactureDate.plusDays(totalDosingDays);
                if (endDate.isBefore(LocalDate.now())) {
                    userPrescription.setIsTaken(true); // 복용 완료
                    userPrescription.setOnAlarm(false); // 지난 처방전이므로 알림 비활성화

                    // 처방전의 약물 리스트에 대해 taken_dosage를 total_dosage와 같게 설정
                    List<UserMedicationEntity> prescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(userPrescription);
                    for (UserMedicationEntity prescriptionDrug : prescriptionDrugs) {
                        prescriptionDrug.setTakenDosingDays(prescriptionDrug.getTotalDosingDays());
                        prescriptionDrug.setTakenDosage(prescriptionDrug.getTotalDosage());
                        prescriptionDrugRepository.save(prescriptionDrug);
                    }
                } else {
                    userPrescription.setIsTaken(false); // 복용 미완료
                    userPrescription.setOnAlarm(true);  // 복용 중이므로 알림 활성화

                    List<UserMedicationEntity> prescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(userPrescription);
                    for (UserMedicationEntity prescriptionDrug : prescriptionDrugs) {
                        // 제조일로부터 경과된 일수 계산
                        long daysSinceManufacture = ChronoUnit.DAYS.between(prescriptionDrug.getPrescriptionId().getManufactureDate(), LocalDate.now());

                        // 경과된 일수가 총 복용 일수보다 적을 경우, 경과된 일수를 taken_dosing_days로 설정
                        int takenDosingDays = (int) Math.min(daysSinceManufacture, prescriptionDrug.getTotalDosingDays());

                        // 복용 일수에 해당하는 taken_dosage 계산
                        int takenDosage = takenDosingDays * prescriptionDrug.getDailyDosesNumber();

                        // 복용 횟수와 총 복용량을 업데이트
                        prescriptionDrug.setTakenDosingDays(takenDosingDays);
                        prescriptionDrug.setTakenDosage(takenDosage);

                        // 변경 사항 저장
                        prescriptionDrugRepository.save(prescriptionDrug);
                    }
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


