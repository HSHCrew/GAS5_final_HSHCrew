package org.zerock.Altari.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.entity.PrescriptionDrugEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.repository.PrescriptionDrugRepository;
import org.zerock.Altari.repository.UserPrescriptionRepository;
import org.zerock.Altari.repository.UserProfileRepository;
import org.zerock.Altari.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class MedicationAlarmService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserPrescriptionRepository userPrescriptionRepository;

    @Autowired
    private PrescriptionDrugRepository prescriptionDrugRepository;

    @Autowired
    private TaskScheduler taskScheduler;


    // 기존 예약된 알람을 저장하기 위한 맵
    private Map<UserEntity, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TwilioCallService twilioCallService;

//    @PostConstruct // 서버가 시작된 후 자동으로 호출
//    public void init() {
//        scheduleAlerts(); // 알림 스케줄링
//    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateTakenDosingDays() {
        List<UserPrescriptionEntity> prescriptions = userPrescriptionRepository.findAll();

        for (UserPrescriptionEntity prescription : prescriptions) {
            List<PrescriptionDrugEntity> drugs = prescriptionDrugRepository.findByPrescriptionId(prescription);

            boolean allDrugsCompleted = true; // 모든 약의 taken_dosing_days가 total_dosing_days와 같은지 확인

            for (PrescriptionDrugEntity drug : drugs) {
                // taken_dosing_days가 total_dosing_days보다 작으면 하루 증가
                if (drug.getTaken_dosing_days() < drug.getTotal_dosing_days()) {
                    drug.setTaken_dosing_days(drug.getTotal_dosing_days() + 1);
                    prescriptionDrugRepository.save(drug);
                }

                // taken_dosing_days가 total_dosing_days와 같지 않으면 모든 약이 완료되지 않음
                if (drug.getTaken_dosing_days() < drug.getTotal_dosing_days()) {
                    allDrugsCompleted = false;
                }
            }

            // 모든 약의 taken_dosing_days가 total_dosing_days에 도달하면 처방전 완료
            if (allDrugsCompleted) {
                prescription.setIsTaken(true);
                userPrescriptionRepository.save(prescription);
            }
        }
    }

    public void scheduleAlerts() {
        List<UserEntity> users = userRepository.findAll();

        for (UserEntity user : users) {
            List<Integer> dosagesCount = setDailyNotificationCount(user);
            UserProfileEntity userProfile = userProfileRepository.findByUsername(user);

            if (userProfile == null) {
                // 유저의 프로필이 없으면 알림 스케줄링을 건너뜀
                cancelScheduledAlerts(user);
                continue;
            }

            if (dosagesCount == null) {
                // 유저가 설정한 알람 시간 혹은 복약 중이지 않을 경우 작업 취소
                cancelScheduledAlerts(user);
                continue;
            }

            int maxDailyDosage = dosagesCount.get(0);
            LocalTime morningMedicationTime = userProfile.getMorning_medication_time();
            LocalTime lunchMedicationTime = userProfile.getLunch_medication_time();
            LocalTime dinnerMedicationTime = userProfile.getDinner_medication_time();
            LocalTime nightMedicationTime = dinnerMedicationTime.plusHours(3);

            // 기존 예약된 알람 취소
            cancelScheduledAlerts(user);

            // 새로운 알람 예약
            ScheduledFuture<?> future = null;

            if (maxDailyDosage == 1) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(lunchMedicationTime)));
            } else if (maxDailyDosage == 2) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(morningMedicationTime)));
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(dinnerMedicationTime)));
            } else if (maxDailyDosage == 3) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(morningMedicationTime)));
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(lunchMedicationTime)));
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(dinnerMedicationTime)));
            } else if (maxDailyDosage == 4) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(morningMedicationTime)));
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(lunchMedicationTime)));
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(dinnerMedicationTime)));
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(nightMedicationTime)));
            }

            // 새로 예약된 작업을 맵에 저장
            scheduledTasks.put(user, future);
        }
    }

    @Transactional
    public void confirmMedication(UserEntity username) {
        UserProfileEntity userProfile = userProfileRepository.findByUsername(username);
        List<UserPrescriptionEntity> activePrescriptions = userPrescriptionRepository.findByUserProfile(userProfile);

        LocalDate today = LocalDate.now(); // 오늘 날짜

        for (UserPrescriptionEntity prescription : activePrescriptions) {
            List<PrescriptionDrugEntity> prescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(prescription);

            boolean allDrugsTaken = true; // 모든 약물이 복용 완료된 상태인지 여부를 추적

            for (PrescriptionDrugEntity prescriptionDrug : prescriptionDrugs) {
                if (prescriptionDrug.getTodayTakenCount() < prescriptionDrug.getDailyDosesNumber()) {
                    prescriptionDrug.setTodayTakenCount(prescriptionDrug.getTodayTakenCount() + 1);
                    prescriptionDrug.setTaken_dosage(prescriptionDrug.getTaken_dosage() + 1);
                    prescriptionDrug.setLastTakenDate(today); // 마지막 복용일 업데이트
                    prescriptionDrugRepository.save(prescriptionDrug);
                }

                // taken_dosage와 total_dosage가 같으면 모든 약물이 완료된 것으로 간주
                if (prescriptionDrug.getTaken_dosage() < prescriptionDrug.getTotal_dosage()) {
                    allDrugsTaken = false; // 한 개라도 복용이 완료되지 않으면 false
                }
            }

            // 모든 약물이 완료되었으면, 처방전의 is_taken을 1로 업데이트
            if (allDrugsTaken) {
                prescription.setIsTaken(true); // is_taken을 1로 설정 (true로 변경)
                userPrescriptionRepository.save(prescription);
            }
        }

        userScheduleAlerts(username);
    }


    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void resetTodayTakenCount() {
        List<PrescriptionDrugEntity> allDrugs = prescriptionDrugRepository.findAll();
        for (PrescriptionDrugEntity drug : allDrugs) {
            drug.setTodayTakenCount(0); // 오늘 복용한 횟수 초기화
            prescriptionDrugRepository.save(drug);
        }
    }

    @Transactional
    public List<Integer> setDailyNotificationCount(UserEntity username) {
        UserProfileEntity userProfile = userProfileRepository.findByUsername(username);
        // is_taken이 false인 활성 상태의 처방전을 조회합니다.
        List<UserPrescriptionEntity> activePrescriptions = userPrescriptionRepository.findByUserProfileAndIsTakenFalse(userProfile);

        int maxDailyDosage = 0; // 초기 하루 복약 알림 횟수
        int maxTotalDays = 0;   // 초기 알림 제공 최대 일수

        boolean allDrugsTaken = true; // 모든 약물 복용 확인 플래그

        // is_taken이 false인 처방전 내 모든 약물에 대해 최대 하루 복용 횟수와 총 복용 일수 설정
        for (UserPrescriptionEntity prescription : activePrescriptions) {
            List<PrescriptionDrugEntity> prescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(prescription);

            for (PrescriptionDrugEntity prescriptionDrug : prescriptionDrugs) {
                // 현재 약물의 총 복용 횟수와 지금까지 복용한 횟수를 비교
                if (prescriptionDrug.getTaken_dosage() < prescriptionDrug.getTotal_dosage()) {
                    // 아직 복용이 끝나지 않은 약물만 고려하여 최대 하루 복용 횟수와 총 복용 일수 갱신
                    allDrugsTaken = false; // 아직 복용이 끝나지 않은 약물이 있음

                    maxDailyDosage = Math.max(maxDailyDosage, prescriptionDrug.getDailyDosesNumber());
                    maxTotalDays = Math.max(maxTotalDays, prescriptionDrug.getTotal_dosing_days());
                }
            }
        }

        // 모든 약물의 복용이 끝났으면 null 반환
        if (allDrugsTaken) {
            return null;
        }

        List<Integer> result = new ArrayList<>();
        result.add(maxDailyDosage);
        result.add(maxTotalDays);

        // NotificationService에서 사용하도록 설정된 알림 횟수와 최대 일수를 전달
        return result;
    }

    private void cancelScheduledAlerts(UserEntity user) {
        ScheduledFuture<?> future = scheduledTasks.remove(user);
        if (future != null) {
            future.cancel(false); // 작업을 취소합니다.
        }
    }

    public void sendDailyMedicationAlerts(UserEntity user) {
        checkMedications(user);
    }

    public void checkMedications(UserEntity username) {
        UserProfileEntity userProfile = userProfileRepository.findByUsername(username);
        String toPhoneNumber = userProfile.getPhone_number(); // 사용자 전화번호
        String messageBody = "안녕하세요! 알타리 서비스 입니다. 지금은 약 복용 시간이니, 잊지 말고 약을 드세요. 건강을 지키는 데 도움이 될 거예요!";

        // 전화 걸기
        twilioCallService.sendCall(toPhoneNumber, messageBody);
    }


    public Map<String, Object> calculateProgressByPrescription(UserEntity userEntity) {
        UserProfileEntity userProfile = userProfileRepository.findByUsername(userEntity);
        List<UserPrescriptionEntity> prescriptions = userPrescriptionRepository.findByUserProfile(userProfile);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> prescriptionProgressList = new ArrayList<>();

        for (UserPrescriptionEntity prescription : prescriptions) {
            int totalTaken = 0;
            int totalRequired = 0;

            List<PrescriptionDrugEntity> medications = prescriptionDrugRepository.findByPrescriptionId(prescription);
            for (PrescriptionDrugEntity medication : medications) {
                totalTaken += medication.getTaken_dosage();
                totalRequired += medication.getTotal_dosage();
            }

            double progress = totalRequired == 0 ? 0 : (double) totalTaken / totalRequired * 100;

            // 각 처방전의 진행률을 담은 Map을 생성하여 리스트에 추가
            Map<String, Object> prescriptionData = new HashMap<>();
            prescriptionData.put("prescriptionId", prescription.getUser_prescription_id());
            prescriptionData.put("progress", progress);
            prescriptionProgressList.add(prescriptionData);
        }

        // 결과 Map에 처방전 리스트 추가
        result.put("prescription_progress: ", prescriptionProgressList);

        return result;
    }

    String createCronExpression(LocalTime alertTime) {
        // LocalTime을 크론 표현식으로 변환
        return String.format("0 %d %d * * ?", alertTime.getMinute(), alertTime.getHour());
    }

    public void userScheduleAlerts(UserEntity user) {
        // 사용자의 복약 알림 예약을 수행
        List<Integer> dosagesCount = setDailyNotificationCount(user);
        UserProfileEntity userProfile = userProfileRepository.findByUsername(user);

        if (dosagesCount == null) {
            // 유저가 설정한 알람 시간 혹은 복약 중이지 않을 경우 작업 취소
            cancelScheduledAlerts(user);
            System.out.println("복약 알림이 취소되었습니다.");
            return;
        }

        int maxDailyDosage = dosagesCount.get(0);
        LocalTime morningMedicationTime = userProfile.getMorning_medication_time();
        LocalTime lunchMedicationTime = userProfile.getLunch_medication_time();
        LocalTime dinnerMedicationTime = userProfile.getDinner_medication_time();
        LocalTime nightMedicationTime = dinnerMedicationTime.plusHours(3);

        // 기존 예약된 알람 취소
        cancelScheduledAlerts(user);

        // 새로운 알람 예약
        ScheduledFuture<?> future = null;
        System.out.println("복약 알림이 재설정 됩니다.");

        if (maxDailyDosage == 1) {
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(lunchMedicationTime)));
            System.out.println("하루 1번 복약 알림이 설정되었습니다.");
        } else if (maxDailyDosage == 2) {
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(morningMedicationTime)));
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(dinnerMedicationTime)));
            System.out.println("하루 2번 복약 알림이 설정되었습니다.");
        } else if (maxDailyDosage == 3) {
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(morningMedicationTime)));
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(lunchMedicationTime)));
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(dinnerMedicationTime)));
            System.out.println("하루 3번 복약 알림이 설정되었습니다.");
        } else if (maxDailyDosage == 4) {
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(morningMedicationTime)));
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(lunchMedicationTime)));
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(dinnerMedicationTime)));
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(nightMedicationTime)));
            System.out.println("하루 4번 복약 알림이 설정되었습니다.");
        }

        // 새로 예약된 작업을 맵에 저장
        scheduledTasks.put(user, future);
    }
}
