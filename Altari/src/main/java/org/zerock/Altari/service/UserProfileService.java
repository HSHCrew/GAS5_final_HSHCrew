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
        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }

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
    public UserProfileDTO updateUserProfile(UserEntity username,
                                            UserProfileDTO userProfileDTO) {

        // 사용자 프로필 조회
        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }

        try {
            UserProfileEntity userProfileEntity = optionalUserProfile.get();

            // DTO의 값으로 프로필 업데이트, 삭제하고 싶은 필드는 null로 설정
            if (userProfileDTO.getFullName() != null) {
                userProfileEntity.setFullName(userProfileDTO.getFullName());
            } else {
                userProfileEntity.setFullName(null); // 사용자가 삭제하고 싶을 경우
            }

            if (userProfileDTO.getDateOfBirth() != null) {
                userProfileEntity.setDateOfBirth(userProfileDTO.getDateOfBirth());
            } else {
                userProfileEntity.setDateOfBirth(null); // 사용자가 삭제하고 싶을 경우
            }

            if (userProfileDTO.getHeight() != null) {
                userProfileEntity.setHeight(userProfileDTO.getHeight());
            } else {
                userProfileEntity.setHeight(null); // 사용자가 삭제하고 싶을 경우
            }

            if (userProfileDTO.getWeight() != null) {
                userProfileEntity.setWeight(userProfileDTO.getWeight());
            } else {
                userProfileEntity.setWeight(null); // 사용자가 삭제하고 싶을 경우
            }

            if (userProfileDTO.getBloodType() != null) {
                userProfileEntity.setBloodType(userProfileDTO.getBloodType());
            } else {
                userProfileEntity.setBloodType(null); // 사용자가 삭제하고 싶을 경우
            }

            if (userProfileDTO.getPhoneNumber() != null) {
                String rawPhoneNumber = userProfileDTO.getPhoneNumber();
                String formattedPhoneNumber = formatPhoneNumber(rawPhoneNumber);
                userProfileEntity.setPhoneNumber(formattedPhoneNumber);
            } else {
                userProfileEntity.setPhoneNumber(null); // 사용자가 삭제하고 싶을 경우
            }
            if (userProfileDTO.getMorningMedicationTime() != null) {
                userProfileEntity.setMorningMedicationTime(userProfileDTO.getMorningMedicationTime());
            } else {
                userProfileEntity.setMorningMedicationTime(null); // 사용자가 삭제하고 싶을 경우
            }
            if (userProfileDTO.getLunchMedicationTime() != null) {
                userProfileEntity.setLunchMedicationTime(userProfileDTO.getLunchMedicationTime());
            } else {
                userProfileEntity.setLunchMedicationTime(null); // 사용자가 삭제하고 싶을 경우
            }
            if (userProfileDTO.getDinnerMedicationTime() != null) {
                userProfileEntity.setDinnerMedicationTime(userProfileDTO.getDinnerMedicationTime());
            } else {
                userProfileEntity.setDinnerMedicationTime(null); // 사용자가 삭제하고 싶을 경우
            }
                // 나머지 필드도 유사하게 처리
                // 필요에 따라 다른 필드도 업데이트

                // 업데이트된 유저 프로필 반환
                userProfileRepository.save(userProfileEntity); // 변경된 엔티티 저장 후 반환

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

        } catch (Exception e) {
            log.error("Error updating user profile", e);
            throw new RuntimeException("Failed to update user profile");


        }

    }
    // 전화번호 포맷
    public String formatPhoneNumber (String phoneNumber){
        if (phoneNumber.startsWith("0")) {
            return "+82" + phoneNumber.substring(1); // "0"을 제거하고 +82 추가
        }
        return phoneNumber; // 이미 국가 코드가 포함된 경우 그대로 반환
    }
}