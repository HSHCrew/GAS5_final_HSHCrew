//package org.zerock.Altari.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.zerock.Altari.entity.PrescriptionDrugEntity;
//import org.zerock.Altari.entity.UserEntity;
//import org.zerock.Altari.entity.UserPrescriptionEntity;
//import org.zerock.Altari.entity.UserProfileEntity;
//import org.zerock.Altari.exception.UserExceptions;
//import org.zerock.Altari.repository.PrescriptionDrugRepository;
//import org.zerock.Altari.repository.UserPrescriptionRepository;
//import org.zerock.Altari.repository.UserProfileRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class MedicationAlertService {
//
//    @Autowired
//    private UserProfileRepository userProfileRepository;
//    @Autowired
//    private PrescriptionDrugRepository prescriptionDrugRepository;
//    @Autowired
//    private UserPrescriptionRepository userPrescriptionRepository;
//
//    public void checkMedicationAndSendAlert(UserEntity username){
//        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
//        if (optionalUserProfile.isEmpty()) {
//            throw UserExceptions.NOT_FOUND.get();
//        }
//
//        List<UserPrescriptionEntity> prescriptions = userPrescriptionRepository.findByUserProfile(optionalUserProfile.get());
//
//    }
//
//
//    public void setAlarmCount(List<PrescriptionDrugEntity> prescriptiondrugs) {
//        if (prescriptiondrugs == null || prescriptiondrugs.isEmpty()) {
//            System.out.println("No prescription drugs found. Set alarm count to: 0");
//            return;
//        }
//
//        // 최대 하루 복용 횟수를 찾기 위한 로직
//        Optional<Integer> maxDailyDosesOpt = prescriptiondrugs.stream()
//                .map(drug -> {
//                    try {
//                        return drug.getDailyDosesNumber(); // String을 int로 변환
//                    } catch (NumberFormatException e) {
//                        return 0; // 변환 실패 시 기본값 0
//                    }
//                })
//                .max(Integer::compareTo); // 최댓값 찾기
//
//        // 최대 하루 복용 횟수 초기화
//        int maxDailyDoses = maxDailyDosesOpt.orElse(0);
//        int alarmCount = 0;
//
//        // 알림 횟수 설정
//        while (true) {
//            // 현재 복용 횟수를 추적하고 있는지 확인
//            boolean allDosesComplete = true;
//
//            for (PrescriptionDrugEntity prescriptionDrug : prescriptiondrugs) {
//
//                int totalDoses = prescriptionDrug.getTotal_dosage(); // 총 복용 횟수
//                int consumedDoses = prescriptionDrug.getTaken_dosage(); // 현재까지 먹은 횟수
//
//                // 총 복용 횟수와 현재 복용 횟수가 같은 경우 카운트 증가 중지
//                if (totalDoses == consumedDoses) {
//                    continue; // 다음 약물로 넘어감
//                } else {
//                    allDosesComplete = false; // 아직 복용이 완료되지 않음
//                }
//
//                // 최대 하루 복용 횟수를 기준으로 알림 횟수 설정
//                if prescriptionDrug.getDailyDosesNumber() == maxDailyDoses) {
//                    alarmCount = maxDailyDoses; // 최대 하루 복용 횟수로 알림 횟수 설정
//                }
//            }
//
//            // 모든 약물의 총 복용 횟수가 완료되었으면 종료
//            if (allDosesComplete) {
//                System.out.println("All doses are complete. No further alarms needed.");
//                break;
//            }
//
//            // 하루 복용 횟수가 가장 큰 데이터의 총 복용 횟수와 현재 복용 횟수가 같은 경우
//            for (PrescriptionDrugEntity prescriptionDrug : prescriptiondrugs) {
//
//
//                if (totalDoses == consumedDoses) {
//                    // 다음으로 하루 복용 횟수가 큰 약물 찾기
//                    Optional<Integer> nextMaxDailyDosesOpt = prescriptiondrugs.stream()
//                            .map(drug -> {
//                                try {
//                                    return drug.getDailyDosesNumber();
//                                } catch (NumberFormatException e) {
//                                    return 0; // 변환 실패 시 기본값 0
//                                }
//                            })
//                            .filter(dailyDose -> dailyDose != maxDailyDoses) // 현재 최대값 제외
//                            .max(Integer::compareTo); // 최댓값 찾기
//
//                    // 다음 최대값으로 알림 횟수 설정
//                    maxDailyDoses = nextMaxDailyDosesOpt.orElse(0);
//                }
//            }
//        }
//
//        // 최종 알림 횟수 출력
//        System.out.println("Alarm count set to: " + alarmCount);
//    }
//}
//
