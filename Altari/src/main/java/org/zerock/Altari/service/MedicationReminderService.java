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
//
//@Service
//@EnableScheduling
//public class MedicationReminderService {
//
//    @Autowired
//    private TaskScheduler taskScheduler;
//
//    @Autowired
//    private UserProfileRepository userProfileRepository; // 사용자의 알람 시간을 가져오기 위한 Repository
//
//    private ScheduledFuture<?> morningTask;
//    private ScheduledFuture<?> lunchTask;
//    private ScheduledFuture<?> eveningTask;
//
//    @PostConstruct
//    public void initializeScheduler() {
//        // DB에서 유저 알람 시간 및 시작일과 종료일 정보를 가져와 스케줄링 설정
//        scheduleAlarms();
//    }
//
//    public void scheduleAlarms() {
//        LocalTime morningTime = userRepository.getMorningAlarmTime(); // DB에서 아침 시간 가져오기
//        LocalTime lunchTime = userRepository.getLunchAlarmTime();     // DB에서 점심 시간 가져오기
//        LocalTime eveningTime = userRepository.getEveningAlarmTime(); // DB에서 저녁 시간 가져오기
//
//        LocalDate startDate = userRepository.getAlarmStartDate(); // DB에서 알람 시작일 가져오기
//        LocalDate endDate = userRepository.getAlarmEndDate();     // DB에서 알람 종료일 가져오기
//
//        // 기존 작업 취소
//        if (morningTask != null) morningTask.cancel(true);
//        if (lunchTask != null) lunchTask.cancel(true);
//        if (eveningTask != null) eveningTask.cancel(true);
//
//        // 알람 설정
//        morningTask = scheduleTaskAtTime(morningTime, startDate, endDate, "아침 알람");
//        lunchTask = scheduleTaskAtTime(lunchTime, startDate, endDate, "점심 알람");
//        eveningTask = scheduleTaskAtTime(eveningTime, startDate, endDate, "저녁 알람");
//    }
//
//    private ScheduledFuture<?> scheduleTaskAtTime(LocalTime time, LocalDate startDate, LocalDate endDate, String message) {
//        // 현재 날짜를 기준으로 알람 시간을 계산
//        LocalDate today = LocalDate.now();
//
//        // 현재 날짜가 시작일과 종료일 사이에 있는지 확인
//        if (today.isBefore(startDate) || today.isAfter(endDate)) {
//            return null; // 설정된 기간 외에는 알람을 전송하지 않음
//        }
//
//        Date alarmTime = java.sql.Timestamp.valueOf(time.atDate(today));
//
//        // 스케줄 설정
//        return taskScheduler.schedule(() -> sendAlarmIfWithinPeriod(startDate, endDate, message), alarmTime);
//    }
//
//    private void sendAlarmIfWithinPeriod(LocalDate startDate, LocalDate endDate, String message) {
//        LocalDate today = LocalDate.now();
//
//        // 오늘 날짜가 알람 전송 기간 내에 있는지 확인
//        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
//            System.out.println("알림: " + message);
//            // 알림 전송 로직 (예: 이메일, 푸시 알림 등)
//        }
//    }
//
//    public void updateAlarmTimes(LocalTime morningTime, LocalTime lunchTime, LocalTime eveningTime, LocalDate startDate, LocalDate endDate) {
//        // 알람 시간을 업데이트하고 DB에 저장한 후 스케줄을 재설정
//        userRepository.updateMorningAlarmTime(morningTime);
//        userRepository.updateLunchAlarmTime(lunchTime);
//        userRepository.updateEveningAlarmTime(eveningTime);
//        userRepository.updateAlarmStartDate(startDate);
//        userRepository.updateAlarmEndDate(endDate);
//
//        // 스케줄 재설정
//        scheduleAlarms();
//    }
//}
//
