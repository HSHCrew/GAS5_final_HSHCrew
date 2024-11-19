package org.zerock.Altari.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserMedicationTimeDTO;
import org.zerock.Altari.dto.UserPrescriptionDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserMedicationTimeEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.UserMedicationTimeRepository;
import org.zerock.Altari.repository.UserProfileRepository;

import java.util.Optional;

@Service
public class UserMedicationTimeService {

    @Autowired
    private UserMedicationTimeRepository userMedicationTimeRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private MedicationAlarmService medicationAlarmService;

    @Transactional
    public UserMedicationTimeDTO updateMedicationAlarmStatus(UserEntity username, UserMedicationTimeDTO userMedicationTimeDTO) {
        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }

        UserMedicationTimeEntity userMedicationTime = userMedicationTimeRepository.findByUserProfile(optionalUserProfile.get());


            // 업데이트 진행
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
            medicationAlarmService.userScheduleAlerts(username);

            // 업데이트된 userMedicationTime의 값을 사용하여 DTO 생성
            UserMedicationTimeDTO userMedicationTimeResult = UserMedicationTimeDTO.builder()
                    .onMorningMedicationAlarm(userMedicationTime.getOnMorningMedicationAlarm())
                    .onLunchMedicationTimeAlarm(userMedicationTime.getOnLunchMedicationTimeAlarm())
                    .onDinnerMedicationTimeAlarm(userMedicationTime.getOnDinnerMedicationTimeAlarm())
                    .onNightMedicationTimeAlarm(userMedicationTime.getOnNightMedicationTimeAlarm())
                    .build();

            return userMedicationTimeDTO;
    }


    // 특정 사용자의 알람 상태 조회
    @Transactional(readOnly = true)
    @Cacheable(value = "medicationTimes", key = "#username")
    public UserMedicationTimeDTO getMedicationTime(UserEntity username) {
        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }
        Optional<UserMedicationTimeEntity> optionalUserMedicationTime = Optional.ofNullable(userMedicationTimeRepository.findByUserProfile(optionalUserProfile.get()));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }

        UserMedicationTimeDTO userMedicationTimeDTO = UserMedicationTimeDTO.builder()
                .onMorningMedicationAlarm(optionalUserMedicationTime.get().getOnMorningMedicationAlarm())
                .onLunchMedicationTimeAlarm(optionalUserMedicationTime.get().getOnLunchMedicationTimeAlarm())
                .onDinnerMedicationTimeAlarm(optionalUserMedicationTime.get().getOnDinnerMedicationTimeAlarm())
                .onNightMedicationTimeAlarm(optionalUserMedicationTime.get().getOnNightMedicationTimeAlarm())
                .build();


        return userMedicationTimeDTO;
    }
}
