package org.zerock.Altari.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.MedicationCompletionDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
@Log4j2
public class MedicationAlarmService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserPrescriptionRepository userPrescriptionRepository;

    @Autowired
    private UserMedicationRepository prescriptionDrugRepository;

    @Autowired
    private TaskScheduler taskScheduler;


    // 기존 예약된 알람을 저장하기 위한 맵
    private Map<UserEntity, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TwilioCallService twilioCallService;
    @Autowired
    private UserMedicationTimeRepository userMedicationTimeRepository;
    @Autowired
    private MedicationCompletionRepository medicationCompletionRepository;

//    @PostConstruct // 서버가 시작된 후 자동으로 호출
//    public void init() {
//        scheduleAlerts(); // 알림 스케줄링
//    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateTakenDosingDays() {
        List<UserPrescriptionEntity> prescriptions = userPrescriptionRepository.findAll();
        List<UserProfileEntity> userProfiles = userProfileRepository.findAll();

        for (UserProfileEntity userProfile : userProfiles) {
            MedicationCompletionEntity medicationCompletion = new MedicationCompletionEntity();
            medicationCompletion.setUserProfile(userProfile);
            medicationCompletion.setCreatedAt(LocalDate.now());
            medicationCompletion.setMorningTaken(false);
            medicationCompletion.setLunchTaken(false);
            medicationCompletion.setDinnerTaken(false);
            medicationCompletion.setNightTaken(false);
            medicationCompletion.setUserProfile(userProfile);

            medicationCompletionRepository.save(medicationCompletion);

            LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

            List<MedicationCompletionEntity> medicationCompletions = medicationCompletionRepository.findByUserProfile(userProfile);

            for (MedicationCompletionEntity medicationCompletionEntity : medicationCompletions) {

                if (medicationCompletionEntity.getCreatedAt().isBefore(threeDaysAgo)) {
                    medicationCompletionRepository.delete(medicationCompletionEntity);  // 3일 전이면 삭제
                }
            }

        }

        for (UserPrescriptionEntity prescription : prescriptions) {
            List<UserMedicationEntity> drugs = prescriptionDrugRepository.findByPrescriptionId(prescription);

            boolean allDrugsCompleted = true; // 모든 약의 taken_dosing_days가 total_dosing_days와 같은지 확인

            for (UserMedicationEntity drug : drugs) {
                // taken_dosing_days가 total_dosing_days보다 작으면 하루 증가
                if (drug.getTakenDosingDays() < drug.getTotalDosingDays()) {
                    drug.setTakenDosingDays(drug.getTotalDosingDays() + 1);
                    prescriptionDrugRepository.save(drug);
                }

                // taken_dosing_days가 total_dosing_days와 같지 않으면 모든 약이 완료되지 않음
                if (drug.getTakenDosingDays() < drug.getTotalDosingDays()) {
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
            UserMedicationTimeEntity userMedicationTime = userMedicationTimeRepository.findByUserProfile(userProfile);

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
            LocalTime morningMedicationTime = userProfile.getMorningMedicationTime();
            LocalTime lunchMedicationTime = userProfile.getLunchMedicationTime();
            LocalTime dinnerMedicationTime = userProfile.getDinnerMedicationTime();
            LocalTime nightMedicationTime = dinnerMedicationTime.plusHours(3);

            // 기존 예약된 알람 취소
            cancelScheduledAlerts(user);

            // 새로운 알람 예약
            ScheduledFuture<?> future = null;

            int MedicationTime = 0;

            if (maxDailyDosage == 1 && Boolean.TRUE.equals(userMedicationTime.getOnLunchMedicationTimeAlarm())) {
                MedicationTime += 1;
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(lunchMedicationTime)));
                System.out.println("하루 " + MedicationTime + "번 복약 알림이 설정되었습니다.");


            } else if (maxDailyDosage == 2) {
                if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                    future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                            new CronTrigger(createCronExpression(morningMedicationTime)));
                    MedicationTime += 1;
                }
                if (Boolean.TRUE.equals(userMedicationTime.getOnDinnerMedicationTimeAlarm())) {
                    future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                            new CronTrigger(createCronExpression(dinnerMedicationTime)));
                    MedicationTime += 1;
                }
                System.out.println("하루 " + MedicationTime + "번 복약 알림이 설정되었습니다.");


            } else if (maxDailyDosage == 3) {
                if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                    future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                            new CronTrigger(createCronExpression(morningMedicationTime)));

                    MedicationTime += 1;
                }
                if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                    future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                            new CronTrigger(createCronExpression(lunchMedicationTime)));
                    MedicationTime += 1;
                }

                if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                    future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                            new CronTrigger(createCronExpression(dinnerMedicationTime)));
                    MedicationTime += 1;
                }

                System.out.println("하루 " + MedicationTime + "번 복약 알림이 설정되었습니다.");

            } else if (maxDailyDosage == 4) {
                if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                    future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                            new CronTrigger(createCronExpression(morningMedicationTime)));
                    MedicationTime += 1;
                }
                if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                    future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                            new CronTrigger(createCronExpression(lunchMedicationTime)));
                    MedicationTime += 1;
                }
                if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                    future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                            new CronTrigger(createCronExpression(dinnerMedicationTime)));
                    MedicationTime += 1;
                }
                if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                    future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                            new CronTrigger(createCronExpression(nightMedicationTime)));
                    MedicationTime += 1;
                }
                System.out.println("하루 " + MedicationTime + "번 복약 알림이 설정되었습니다.");
            }

            // 새로 예약된 작업을 맵에 저장
            scheduledTasks.put(user, future);
        }
    }

    @Transactional
    public void confirmMedication(UserEntity username,
                                  MedicationCompletionDTO medicationCompletionDTO
    ) {
        UserProfileEntity userProfile = userProfileRepository.findByUsername(username);
        List<UserPrescriptionEntity> activePrescriptions = userPrescriptionRepository.findByUserProfile(userProfile);
        List<MedicationCompletionEntity> medicationCompletions = medicationCompletionRepository.findByUserProfile(userProfile);

        LocalDate today = LocalDate.now(); // 오늘 날짜

        for (MedicationCompletionEntity medicationCompletion : medicationCompletions) {

            LocalDate createTime = medicationCompletion.getCreatedAt();

            if (createTime.isEqual(today)) {
                if (medicationCompletionDTO.getMorningTaken()) {
                    medicationCompletion.setMorningTaken(true);
                }
                if (medicationCompletionDTO.getLunchTaken()) {
                    medicationCompletion.setLunchTaken(true);
                }
                if (medicationCompletionDTO.getDinnerTaken()) {
                    medicationCompletion.setDinnerTaken(true);
                }
                if (medicationCompletionDTO.getNightTaken()) {
                    medicationCompletion.setNightTaken(true);
                }
                // createTime이 오늘인 경우 처리할 로직
                medicationCompletionRepository.save(medicationCompletion);
            }

        }


        for (UserPrescriptionEntity prescription : activePrescriptions) {

            List<UserMedicationEntity> prescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(prescription);

            boolean allDrugsTaken = true; // 모든 약물이 복용 완료된 상태인지 여부를 추적

            for (UserMedicationEntity prescriptionDrug : prescriptionDrugs) {
                if (prescriptionDrug.getTodayTakenCount() < prescriptionDrug.getDailyDosesNumber()) {
                    prescriptionDrug.setTodayTakenCount(prescriptionDrug.getTodayTakenCount() + 1);
                    prescriptionDrug.setTakenDosage(prescriptionDrug.getTakenDosage() + 1);
                    prescriptionDrug.setLastTakenDate(today); // 마지막 복용일 업데이트
                    prescriptionDrugRepository.save(prescriptionDrug);
                }

                // taken_dosage와 total_dosage가 같으면 모든 약물이 완료된 것으로 간주
                if (prescriptionDrug.getTakenDosage() < prescriptionDrug.getTotalDosage()) {
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
        List<UserMedicationEntity> allDrugs = prescriptionDrugRepository.findAll();
        for (UserMedicationEntity drug : allDrugs) {
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
            if (prescription.getOnAlarm()) {
                List<UserMedicationEntity> prescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(prescription);

                for (UserMedicationEntity prescriptionDrug : prescriptionDrugs) {
                    // 현재 약물의 총 복용 횟수와 지금까지 복용한 횟수를 비교
                    if (prescriptionDrug.getTakenDosage() < prescriptionDrug.getTotalDosage()) {
                        // 아직 복용이 끝나지 않은 약물만 고려하여 최대 하루 복용 횟수와 총 복용 일수 갱신
                        allDrugsTaken = false; // 아직 복용이 끝나지 않은 약물이 있음

                        maxDailyDosage = Math.max(maxDailyDosage, prescriptionDrug.getDailyDosesNumber());
                        maxTotalDays = Math.max(maxTotalDays, prescriptionDrug.getTotalDosingDays());
                    }
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
        String toPhoneNumber = userProfile.getPhoneNumber(); // 사용자 전화번호
        String messageBody = "안녕하세요! 알타리 서비스 입니다. 지금은 약 복용 시간이니, 잊지 말고 약을 드세요. 건강을 지키는 데 도움이 될 거예요!";

        // 전화 걸기
        twilioCallService.sendCall(toPhoneNumber, messageBody);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userMedicationAlarm", key = "#username")
    public Map<String, Object> calculateProgressByPrescription(UserEntity username) {
        UserProfileEntity userProfile = userProfileRepository.findByUsername(username);
        List<UserPrescriptionEntity> prescriptions = userPrescriptionRepository.findByUserProfile(userProfile);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> prescriptionProgressList = new ArrayList<>();

        for (UserPrescriptionEntity prescription : prescriptions) {
            int totalTaken = 0;
            int totalRequired = 0;

            List<UserMedicationEntity> medications = prescriptionDrugRepository.findByPrescriptionId(prescription);
            for (UserMedicationEntity medication : medications) {
                totalTaken += medication.getTakenDosage();
                totalRequired += medication.getTotalDosage();
            }

            double progress = totalRequired == 0 ? 0 : (double) totalTaken / totalRequired * 100;

            // 각 처방전의 진행률을 담은 Map을 생성하여 리스트에 추가
            Map<String, Object> prescriptionData = new HashMap<>();
            prescriptionData.put("prescriptionId", prescription.getUserPrescriptionId());
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

    @Transactional
    public void userScheduleAlerts(UserEntity user) {
        // 사용자의 복약 알림 예약을 수행
        List<Integer> dosagesCount = setDailyNotificationCount(user);
        UserProfileEntity userProfile = userProfileRepository.findByUsername(user);
        UserMedicationTimeEntity userMedicationTime = userMedicationTimeRepository.findByUserProfile(userProfile);

        if (dosagesCount == null) {
            // 유저가 설정한 알람 시간 혹은 복약 중이지 않을 경우 작업 취소
            cancelScheduledAlerts(user);
            System.out.println("복약 알림이 취소되었습니다.");
            return;
        }

        int maxDailyDosage = dosagesCount.get(0);
        LocalTime morningMedicationTime = userProfile.getMorningMedicationTime();
        LocalTime lunchMedicationTime = userProfile.getLunchMedicationTime();
        LocalTime dinnerMedicationTime = userProfile.getDinnerMedicationTime();
        LocalTime nightMedicationTime = dinnerMedicationTime.plusHours(3);

        // 기존 예약된 알람 취소
        cancelScheduledAlerts(user);

        // 새로운 알람 예약
        ScheduledFuture<?> future = null;
        System.out.println("복약 알림이 재설정 됩니다.");

        int MedicationTime = 0;

        if (maxDailyDosage == 1 && Boolean.TRUE.equals(userMedicationTime.getOnLunchMedicationTimeAlarm())) {
            MedicationTime += 1;
            future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                    new CronTrigger(createCronExpression(lunchMedicationTime)));
            System.out.println("하루 " + MedicationTime + "번 복약 알림이 설정되었습니다.");


        } else if (maxDailyDosage == 2) {
            if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(morningMedicationTime)));
                MedicationTime += 1;
            }
            if (Boolean.TRUE.equals(userMedicationTime.getOnDinnerMedicationTimeAlarm())) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(dinnerMedicationTime)));
                MedicationTime += 1;
            }
            System.out.println("하루 " + MedicationTime + "번 복약 알림이 설정되었습니다.");


        } else if (maxDailyDosage == 3) {
            if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(morningMedicationTime)));

                MedicationTime += 1;
            }
            if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(lunchMedicationTime)));
                MedicationTime += 1;
            }

            if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(dinnerMedicationTime)));
                MedicationTime += 1;
            }

            System.out.println("하루 " + MedicationTime + "번 복약 알림이 설정되었습니다.");

        } else if (maxDailyDosage == 4) {
            if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(morningMedicationTime)));
                MedicationTime += 1;
            }
            if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(lunchMedicationTime)));
                MedicationTime += 1;
            }
            if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(dinnerMedicationTime)));
                MedicationTime += 1;
            }
            if (Boolean.TRUE.equals(userMedicationTime.getOnMorningMedicationAlarm())) {
                future = taskScheduler.schedule(() -> sendDailyMedicationAlerts(user),
                        new CronTrigger(createCronExpression(nightMedicationTime)));
                MedicationTime += 1;
            }
            System.out.println("하루 " + MedicationTime + "번 복약 알림이 설정되었습니다.");
        }

        // 새로 예약된 작업을 맵에 저장
        scheduledTasks.put(user, future);
    }

    public Boolean onAlarm(UserEntity user, Boolean onAlarm) {

        if (onAlarm) {
            userScheduleAlerts(user);
            return true;
        } else {
            cancelScheduledAlerts(user);
            return false;
        }

    }

    @Transactional(readOnly = true)
    @Cacheable(value = "medicationCompletions", key = "#user")
    public List<MedicationCompletionDTO> getMedicationCompletion(UserEntity user) {
        UserProfileEntity userProfile = userProfileRepository.findByUsername(user);
        List<MedicationCompletionEntity> medicationCompletions = medicationCompletionRepository.findByUserProfile(userProfile);
        List<MedicationCompletionDTO> medicationCompletionList = new ArrayList<>();

        // 각 MedicationCompletionEntity를 MedicationCompletionDTO로 변환
        try {
            for (MedicationCompletionEntity entity : medicationCompletions) {
                MedicationCompletionDTO dto = MedicationCompletionDTO.builder()
                        .morningTaken(entity.getMorningTaken())
                        .lunchTaken(entity.getLunchTaken())
                        .dinnerTaken(entity.getDinnerTaken())
                        .nightTaken(entity.getNightTaken())
                        .build();

                medicationCompletionList.add(dto);
            }

            return medicationCompletionList;
        } catch (Exception e) {
            log.error("Error get medicationCompletions", e);
            throw new RuntimeException("Error get medicationCompletions");
        }
    }
}


