package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserProfileDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.UserProfileRepository;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfileDTO getUserProfile(UserEntity username) {
        // findByUsername을 한 번만 호출하고 Optional을 처리
        UserProfileEntity userProfileEntity = userProfileRepository.findByUsername(username);

        return UserProfileDTO.builder()
                .userProfileId(userProfileEntity.getUserProfileId())
                .fullName(userProfileEntity.getFullName())
                .dateOfBirth(userProfileEntity.getDateOfBirth())
                .phoneNumber(userProfileEntity.getPhoneNumber())
                .height(userProfileEntity.getHeight())
                .weight(userProfileEntity.getWeight())
                .bloodType(userProfileEntity.getBloodType())
                .morningMedicationTime(userProfileEntity.getMorningMedicationTime())
                .lunchMedicationTime(userProfileEntity.getLunchMedicationTime())
                .dinnerMedicationTime(userProfileEntity.getDinnerMedicationTime())
                .user_profile_created_at(userProfileEntity.getUser_profile_created_at())
                .user_profile_updated_at(userProfileEntity.getUser_profile_updated_at())
                .build();
    }

    @Transactional
    public UserProfileDTO updateUserProfile(UserEntity username, UserProfileDTO userProfileDTO) {
        // 프로필 조회: Optional로 처리
        UserProfileEntity userProfileEntity = userProfileRepository.findByUsername(username);

        // DTO를 기반으로 값이 있으면 업데이트, 없으면 null로 처리
        updateField(userProfileDTO.getFullName(), userProfileEntity::setFullName);
        updateField(userProfileDTO.getDateOfBirth(), userProfileEntity::setDateOfBirth);
        updateField(userProfileDTO.getHeight(), userProfileEntity::setHeight);
        updateField(userProfileDTO.getWeight(), userProfileEntity::setWeight);
        updateField(userProfileDTO.getBloodType(), userProfileEntity::setBloodType);
        updatePhoneNumber(userProfileDTO.getPhoneNumber(), userProfileEntity);
        updateField(userProfileDTO.getMorningMedicationTime(), userProfileEntity::setMorningMedicationTime);
        updateField(userProfileDTO.getLunchMedicationTime(), userProfileEntity::setLunchMedicationTime);
        updateField(userProfileDTO.getDinnerMedicationTime(), userProfileEntity::setDinnerMedicationTime);

        // 프로필 업데이트 후 저장
        userProfileRepository.save(userProfileEntity);

        return UserProfileDTO.builder()
                .userProfileId(userProfileEntity.getUserProfileId())
                .fullName(userProfileEntity.getFullName())
                .dateOfBirth(userProfileEntity.getDateOfBirth())
                .phoneNumber(userProfileEntity.getPhoneNumber())
                .height(userProfileEntity.getHeight())
                .weight(userProfileEntity.getWeight())
                .bloodType(userProfileEntity.getBloodType())
                .morningMedicationTime(userProfileEntity.getMorningMedicationTime())
                .lunchMedicationTime(userProfileEntity.getLunchMedicationTime())
                .dinnerMedicationTime(userProfileEntity.getDinnerMedicationTime())
                .user_profile_created_at(userProfileEntity.getUser_profile_created_at())
                .user_profile_updated_at(userProfileEntity.getUser_profile_updated_at())
                .build();
    }

    // 필드를 업데이트하는 메서드
    private <T> void updateField(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    // 전화번호 포맷 처리
    private void updatePhoneNumber(String phoneNumber, UserProfileEntity userProfileEntity) {
        if (phoneNumber != null) {
            userProfileEntity.setPhoneNumber(formatPhoneNumber(phoneNumber));
        } else {
            userProfileEntity.setPhoneNumber(null);
        }
    }

    // 전화번호 포맷
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.startsWith("0")) {
            return "+82" + phoneNumber.substring(1); // "0"을 제거하고 +82 추가
        }
        return phoneNumber; // 이미 국가 코드가 포함된 경우 그대로 반환
    }
}
