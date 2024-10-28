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

        UserProfileEntity userProfileEntity = userProfileRepository.findByUsername(username);

        return UserProfileDTO.builder()
                .user_profile_id(userProfileEntity.getUser_profile_id())
                .full_name(userProfileEntity.getFull_name())
                .date_of_birth(userProfileEntity.getDate_of_birth())
                .phone_number(userProfileEntity.getPhone_number())
                .height(userProfileEntity.getHeight())
                .weight(userProfileEntity.getWeight())
                .blood_type(userProfileEntity.getBlood_type())
                .morning_medication_time(userProfileEntity.getMorning_medication_time())
                .lunch_medication_time(userProfileEntity.getLunch_medication_time())
                .dinner_medication_time(userProfileEntity.getDinner_medication_time())
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
            if (userProfileDTO.getFull_name() != null) {
                userProfileEntity.setFull_name(userProfileDTO.getFull_name());
            } else {
                userProfileEntity.setFull_name(null); // 사용자가 삭제하고 싶을 경우
            }

            if (userProfileDTO.getDate_of_birth() != null) {
                userProfileEntity.setDate_of_birth(userProfileDTO.getDate_of_birth());
            } else {
                userProfileEntity.setDate_of_birth(null); // 사용자가 삭제하고 싶을 경우
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

            if (userProfileDTO.getBlood_type() != null) {
                userProfileEntity.setBlood_type(userProfileDTO.getBlood_type());
            } else {
                userProfileEntity.setBlood_type(null); // 사용자가 삭제하고 싶을 경우
            }

            if (userProfileDTO.getPhone_number() != null) {
                userProfileEntity.setPhone_number(userProfileDTO.getPhone_number());
            } else {
                userProfileEntity.setPhone_number(null); // 사용자가 삭제하고 싶을 경우
            }
            if (userProfileDTO.getMorning_medication_time() != null) {
                userProfileEntity.setMorning_medication_time(userProfileDTO.getMorning_medication_time());
            } else {
                userProfileEntity.setMorning_medication_time(null); // 사용자가 삭제하고 싶을 경우
            }
            if (userProfileDTO.getLunch_medication_time() != null) {
                userProfileEntity.setLunch_medication_time(userProfileDTO.getLunch_medication_time());
            } else {
                userProfileEntity.setLunch_medication_time(null); // 사용자가 삭제하고 싶을 경우
            }
            if (userProfileDTO.getDinner_medication_time() != null) {
                userProfileEntity.setDinner_medication_time(userProfileDTO.getDinner_medication_time());
            } else {
                userProfileEntity.setDinner_medication_time(null); // 사용자가 삭제하고 싶을 경우
            }

            // 나머지 필드도 유사하게 처리
            // 필요에 따라 다른 필드도 업데이트

            // 업데이트된 유저 프로필 반환
            userProfileRepository.save(userProfileEntity); // 변경된 엔티티 저장 후 반환

            return UserProfileDTO.builder()
                    .user_profile_id(userProfileEntity.getUser_profile_id())
                    .full_name(userProfileEntity.getFull_name())
                    .date_of_birth(userProfileEntity.getDate_of_birth())
                    .phone_number(userProfileEntity.getPhone_number())
                    .height(userProfileEntity.getHeight())
                    .weight(userProfileEntity.getWeight())
                    .blood_type(userProfileEntity.getBlood_type())
                    .morning_medication_time(userProfileEntity.getMorning_medication_time())
                    .lunch_medication_time(userProfileEntity.getLunch_medication_time())
                    .dinner_medication_time(userProfileEntity.getDinner_medication_time())
                    .user_profile_created_at(userProfileEntity.getUser_profile_created_at())
                    .user_profile_updated_at(userProfileEntity.getUser_profile_updated_at())
                    .build();

        } catch (Exception e) {
            log.error("Error updating user profile", e);
            throw new RuntimeException("Failed to update user profile");
        }
    }
}

