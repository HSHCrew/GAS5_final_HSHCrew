package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.RegisterDTO;
import org.zerock.Altari.dto.UserDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;
    private final AllergyRepository allergyRepository;
    private final MedicationRepository medicationRepository;
    private final UserDiseaseRepository userDiseaseRepository;
    private final UserPastDiseaseRepository userPastDiseaseRepository;
    private final FamilyHistoryRepository familyHistoryRepository;


    public UserDTO read(String username, String password) {
        Optional<UserEntity> result = userRepository.findById(username); //
        UserEntity userEntity = result.orElseThrow(UserExceptions.BAD_CREDENTIALS::get);

        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw UserExceptions.BAD_CREDENTIALS.get();
        }

        return new UserDTO(userEntity);
    }

    public UserDTO getByUsername(String username) {
        Optional<UserEntity> result = userRepository.findById(username);
        UserEntity userEntity = result.orElseThrow(UserExceptions.NOT_FOUND::get);

        return new UserDTO(userEntity);
    }

    @Transactional
    public UserEntity join(RegisterDTO registerDTO) {
        // 1. 유저의 존재에 대한 검증
        Optional<UserEntity> optionalUser = Optional.ofNullable(userRepository.findByUsername(registerDTO.getUsername()));
        if (optionalUser.isPresent()) {
            UserEntity existingUser = optionalUser.get();
            throw new RuntimeException(existingUser.getUsername() + "는 이미 존재하는 아이디입니다.");
        }

        // 2. 유저 정보 저장
        UserEntity user = UserEntity.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .role(registerDTO.getRole())
                .build();

        UserEntity savedUser = userRepository.save(user);
        // 2. 유저 프로필 저장
        UserProfileEntity userProfile = UserProfileEntity.builder()
                .full_name(registerDTO.getFull_name())
                .date_of_birth(registerDTO.getDate_of_birth())
                .phone_number(registerDTO.getPhone_number())
                .height(registerDTO.getHeight())
                .weight(registerDTO.getWeight())
                .blood_type(registerDTO.getBlood_type())
                .morning_medication_time(registerDTO.getMorning_medication_time())
                .lunch_medication_time(registerDTO.getLunch_medication_time())
                .dinner_medication_time(registerDTO.getDinner_medication_time())
                .username(savedUser)
                .build();


        UserProfileEntity savedUserProfile = userProfileRepository.save(userProfile);

        UserDiseaseEntity userDisease = UserDiseaseEntity.builder()
                .disease_id(registerDTO.getDisease_id())
                .user_profile_id(savedUserProfile)
                .build();

        UserDiseaseEntity savedUserDisease = userDiseaseRepository.save(userDisease);

        UserPastDiseaseEntity userPastDisease = UserPastDiseaseEntity.builder()
                .disease_id(registerDTO.getDisease_id())
                .user_profile_id(savedUserProfile)
                .build();

        UserPastDiseaseEntity saveduserPastDisease = userPastDiseaseRepository.save(userPastDisease);

        FamilyHistoryEntity familyHistory = FamilyHistoryEntity.builder()
                .disease_id(registerDTO.getDisease_id())
                .user_profile_id(savedUserProfile)
                .family_relation(registerDTO.getFamily_relation())
                .build();

        FamilyHistoryEntity savedfamilyHistory = familyHistoryRepository.save(familyHistory);

        AllergyEntity allergy = AllergyEntity.builder()
                .medication_id(registerDTO.getMedication_id())
                .food_name(registerDTO.getFood_name())
                .user_profile_id(savedUserProfile)
                .build();

        AllergyEntity savedallergy = allergyRepository.save(allergy);

        return savedUser;
    }
    public boolean isUsernameDuplicate(String username) {
        // username으로 UserEntity를 찾음
        UserEntity user = userRepository.findByUsername(username);

        // user가 null이면 중복되지 않음, 아니면 중복됨
        return user != null; // 중복이면 true, 아니면 false
    }


}

