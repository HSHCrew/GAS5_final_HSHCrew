package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserProfileDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserMedicationTimeEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.UserMedicationTimeRepository;
import org.zerock.Altari.repository.UserProfileRepository;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
@CacheConfig(cacheNames = "userProfile")
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserMedicationTimeRepository userMedicationTimeRepository;
    private final MedicationAlarmService medicationAlarmService;


    @Transactional(readOnly = true)
    @Cacheable(key = "#username")
    public UserProfileDTO getUserProfile(UserEntity username) {

        UserProfileEntity userProfileEntity = userProfileRepository.findByUser(username).orElseThrow(UserExceptions.NOT_FOUND::get);

        return UserProfileDTO.builder().userProfileId(userProfileEntity.getUserProfileId()).fullName(userProfileEntity.getFullName()).dateOfBirth(userProfileEntity.getDateOfBirth()).phoneNumber(userProfileEntity.getPhoneNumber()).height(userProfileEntity.getHeight()).weight(userProfileEntity.getWeight()).bloodType(userProfileEntity.getBloodType()).morningMedicationTime(userProfileEntity.getMorningMedicationTime()).lunchMedicationTime(userProfileEntity.getLunchMedicationTime()).dinnerMedicationTime(userProfileEntity.getDinnerMedicationTime()).user_profile_created_at(userProfileEntity.getUser_profile_created_at()).user_profile_updated_at(userProfileEntity.getUser_profile_updated_at())
                .profileImage(userProfileEntity.getProfileImage())
                .build();

    }


    @Transactional
    @CacheEvict(key = "#username.username")  // 캐시 갱신
    public UserProfileDTO updateUserProfile(UserEntity user, UserProfileDTO userProfileDTO) {

        // 사용자 프로필 조회
        UserProfileEntity userProfileEntity = userProfileRepository.findByUser(user).orElseThrow(UserExceptions.NOT_FOUND::get);

        // DTO의 값으로 프로필 업데이트, 삭제하고 싶은 필드는 null로 설정
        if (userProfileDTO.getFullName() != null) {
            userProfileEntity.setFullName(userProfileDTO.getFullName());
        }

        if (userProfileDTO.getDateOfBirth() != null) {
            userProfileEntity.setDateOfBirth(userProfileDTO.getDateOfBirth());
        }

        if (userProfileDTO.getHeight() != null) {
            userProfileEntity.setHeight(userProfileDTO.getHeight());
        }

        if (userProfileDTO.getWeight() != null) {
            userProfileEntity.setWeight(userProfileDTO.getWeight());
        }

        if (userProfileDTO.getBloodType() != null) {
            userProfileEntity.setBloodType(userProfileDTO.getBloodType());
        }

        if (userProfileDTO.getPhoneNumber() != null) {
            String rawPhoneNumber = userProfileDTO.getPhoneNumber();
            String formattedPhoneNumber = formatPhoneNumber(rawPhoneNumber);
            userProfileEntity.setPhoneNumber(formattedPhoneNumber);
        }
        if (userProfileDTO.getMorningMedicationTime() != null) {
            userProfileEntity.setMorningMedicationTime(userProfileDTO.getMorningMedicationTime());
        }

        if (userProfileDTO.getLunchMedicationTime() != null) {
            userProfileEntity.setLunchMedicationTime(userProfileDTO.getLunchMedicationTime());

        }
        if (userProfileDTO.getDinnerMedicationTime() != null) {
            userProfileEntity.setDinnerMedicationTime(userProfileDTO.getDinnerMedicationTime());

        }

        if (userProfileDTO.getProfileImage() != null) {
            userProfileEntity.setProfileImage(userProfileDTO.getProfileImage());

        }

        medicationAlarmService.userScheduleAlerts(user);

        // 업데이트된 유저 프로필 반환
        userProfileRepository.save(userProfileEntity); // 변경된 엔티티 저장 후 반환

        return UserProfileDTO.builder().userProfileId(userProfileEntity.getUserProfileId()).fullName(userProfileEntity.getFullName()).dateOfBirth(userProfileEntity.getDateOfBirth()).phoneNumber(userProfileEntity.getPhoneNumber()).height(userProfileEntity.getHeight()).weight(userProfileEntity.getWeight()).bloodType(userProfileEntity.getBloodType()).morningMedicationTime(userProfileEntity.getMorningMedicationTime()).lunchMedicationTime(userProfileEntity.getLunchMedicationTime()).dinnerMedicationTime(userProfileEntity.getDinnerMedicationTime()).user_profile_created_at(userProfileEntity.getUser_profile_created_at()).user_profile_updated_at(userProfileEntity.getUser_profile_updated_at()).profileImage(userProfileEntity.getProfileImage()).build();

    }

    // 전화번호 포맷
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.startsWith("0")) {
            return "+82" + phoneNumber.substring(1); // "0"을 제거하고 +82 추가
        }
        return phoneNumber; // 이미 국가 코드가 포함된 경우 그대로 반환
    }
}