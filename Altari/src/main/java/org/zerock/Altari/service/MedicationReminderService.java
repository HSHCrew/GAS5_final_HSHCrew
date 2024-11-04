//package org.zerock.Altari.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.TaskScheduler;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.stereotype.Service;
//import org.zerock.Altari.repository.UserProfileRepository;
//
//import javax.annotation.PostConstruct;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.Date;
//import java.util.concurrent.ScheduledFuture;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//@EnableScheduling
//public class MedicationReminderService {
//
//    @Autowired
//    private TaskScheduler taskScheduler;
//
//    @Autowired
//    private UserProfileRepository userProfileRepository;
//
//    // 사용자 ID를 키로 하는 ScheduledFuture를 저장하는 맵
//    private Map<Long, ScheduledFuture<?>> morningTasks = new ConcurrentHashMap<>();
//    private Map<Long, ScheduledFuture<?>> lunchTasks = new ConcurrentHashMap<>();
//    private Map<Long, ScheduledFuture<?>> eveningTasks = new ConcurrentHashMap<>();
//
//    @PostConstruct
//    public void initializeScheduler() {
//        // 모든 사용자에 대한 알람 스케줄링
//        scheduleAlarmsForAllUsers();
//    }
//
//    private void scheduleAlarmsForAllUsers() {
//        userProfileRepository.findAll().forEach(userProfile -> {
//            scheduleAlarmsForUser(userProfile.getUser_profile_id());
//        });
//    }
//
//    public void scheduleAlarmsForUser(Integer userProfileId) {
//
//
//        LocalTime morningTime = userProfileRepository.getMorningAlarmTime(userId);
//        LocalTime lunchTime = userProfileRepository.getLunchAlarmTime(userId);
//        LocalTime eveningTime = userProfileRepository.getEveningAlarmTime(userId);
//
//        LocalDate startDate = userProfileRepository.getAlarmStartDate(userId);
//        LocalDate endDate = userProfileRepository.getAlarmEndDate(userId);
//
//        // 기존 작업 취소
//        cancelExistingTasks(userId);
//
//        // 알람 설정
//        morningTasks.put(userId, scheduleTaskAtTime(morningTime, startDate, endDate, userId, "아침 알람"));
//        lunchTasks.put(userId, scheduleTaskAtTime(lunchTime, startDate, endDate, userId, "점심 알람"));
//        eveningTasks.put(userId, scheduleTaskAtTime(eveningTime, startDate, endDate, userId, "저녁 알람"));
//    }
//
//    private void cancelExistingTasks(Long userId) {
//        if (morningTasks.containsKey(userId)) {
//            morningTasks.get(userId).cancel(true);
//            morningTasks.remove(userId);
//        }
//        if (lunchTasks.containsKey(userId)) {
//            lunchTasks.get(userId).cancel(true);
//            lunchTasks.remove(userId);
//        }
//        if (eveningTasks.containsKey(userId)) {
//            eveningTasks.get(userId).cancel(true);
//            eveningTasks.remove(userId);
//        }
//    }
//
//    private ScheduledFuture<?> scheduleTaskAtTime(LocalTime time, LocalDate startDate, LocalDate endDate, Long userId, String message) {
//        LocalDate today = LocalDate.now();
//
//        if (today.isBefore(startDate) || today.isAfter(endDate)) {
//            return null; // 설정된 기간 외에는 알람을 전송하지 않음
//        }
//
//        Date alarmTime = java.sql.Timestamp.valueOf(time.atDate(today));
//
//        // 스케줄 설정
//        return taskScheduler.schedule(() -> sendAlarmIfWithinPeriod(startDate, endDate, message, userId), alarmTime);
//    }
//
//    private void sendAlarmIfWithinPeriod(LocalDate startDate, LocalDate endDate, String message, Long userId) {
//        LocalDate today = LocalDate.now();
//
//        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
//            System.out.println("사용자 " + userId + "의 알림: " + message);
//            // 알림 전송 로직 (예: 이메일, 푸시 알림 등)
//        }
//    }
//
//    public void updateAlarmTimes(Long userId, LocalTime morningTime, LocalTime lunchTime, LocalTime eveningTime, LocalDate startDate, LocalDate endDate) {
//        // 알람 시간을 업데이트하고 DB에 저장한 후 스케줄을 재설정
//        userProfileRepository.updateMorningAlarmTime(userId, morningTime);
//        userProfileRepository.updateLunchAlarmTime(userId, lunchTime);
//        userProfileRepository.updateEveningAlarmTime(userId, eveningTime);
//        userProfileRepository.updateAlarmStartDate(userId, startDate);
//        userProfileRepository.updateAlarmEndDate(userId, endDate);
//
//        // 스케줄 재설정
//        scheduleAlarmsForUser(userId);
//    }
//}
//
