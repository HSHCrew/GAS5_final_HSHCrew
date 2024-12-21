package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserMedicationTimeDTO;
import org.zerock.Altari.dto.UserPrescriptionDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserMedicationTimeEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.UserMedicationTimeRepository;
import org.zerock.Altari.repository.UserProfileRepository;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = "userMedicationTime")
public class UserMedicationTimeService {

    @Autowired
    private UserMedicationTimeRepository userMedicationTimeRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private MedicationAlarmService medicationAlarmService;

    @Transactional
    @CacheEvict(key = "#username")
    public UserMedicationTimeDTO updateMedicationAlarmStatus(UserEntity user, UserMedicationTimeDTO userMedicationTimeDTO) {

        UserMedicationTimeEntity userMedicationTime = userMedicationTimeRepository.findByUser(user).orElseThrow(UserExceptions.NOT_FOUND::get);

        if (userMedicationTimeDTO.getOnMorningMedicationAlarm() != null) {
            userMedicationTime.setOnMorningMedicationAlarm(userMedicationTimeDTO.getOnMorningMedicationAlarm());
        }
        if (userMedicationTimeDTO.getOnLunchMedicationTimeAlarm() != null) {
            userMedicationTime.setOnLunchMedicationTimeAlarm(userMedicationTimeDTO.getOnLunchMedicationTimeAlarm());
        }
        if (userMedicationTimeDTO.getOnDinnerMedicationTimeAlarm() != null) {
            userMedicationTime.setOnDinnerMedicationTimeAlarm(userMedicationTimeDTO.getOnDinnerMedicationTimeAlarm());
        }
        if (userMedicationTimeDTO.getOnNightMedicationTimeAlarm() != null) {
            userMedicationTime.setOnNightMedicationTimeAlarm(userMedicationTimeDTO.getOnNightMedicationTimeAlarm());
        }

        userMedicationTimeRepository.save(userMedicationTime);
        medicationAlarmService.userScheduleAlerts(user);

        return UserMedicationTimeDTO.builder().onMorningMedicationAlarm(userMedicationTime.getOnMorningMedicationAlarm()).onLunchMedicationTimeAlarm(userMedicationTime.getOnLunchMedicationTimeAlarm()).onDinnerMedicationTimeAlarm(userMedicationTime.getOnDinnerMedicationTimeAlarm()).onNightMedicationTimeAlarm(userMedicationTime.getOnNightMedicationTimeAlarm()).build();
    }


    // 특정 사용자의 알람 상태 조회
    @Transactional(readOnly = true)
    @Cacheable(key = "#username")
    public UserMedicationTimeDTO getMedicationTime(UserEntity user) {

        Optional<UserMedicationTimeEntity> optionalUserMedicationTime = userMedicationTimeRepository.findByUser(user);
        UserMedicationTimeEntity userMedicationTime = optionalUserMedicationTime.orElseThrow(UserExceptions.NOT_FOUND::get);

        return UserMedicationTimeDTO.builder().onMorningMedicationAlarm(userMedicationTime.getOnMorningMedicationAlarm()).onLunchMedicationTimeAlarm(userMedicationTime.getOnLunchMedicationTimeAlarm()).onDinnerMedicationTimeAlarm(userMedicationTime.getOnDinnerMedicationTimeAlarm()).onNightMedicationTimeAlarm(userMedicationTime.getOnNightMedicationTimeAlarm()).build();
    }
}
