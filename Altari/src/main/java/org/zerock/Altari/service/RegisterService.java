package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.RegisterDTO;
import org.zerock.Altari.dto.UserDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.exception.RegisterExceptions;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;
    private final AllergyRepository allergyRepository;
    private final MedicationRepository medicationRepository;
    private final UserDiseaseRepository userDiseaseRepository;
    private final UserPastDiseaseRepository userPastDiseaseRepository;
    private final FamilyHistoryRepository familyHistoryRepository;



    @Transactional
    public UserEntity join(RegisterDTO registerDTO) {
        // 1. 유저의 존재에 대한 검증
        Optional<UserEntity> optionalUser = Optional.ofNullable(userRepository.findByUsername(registerDTO.getUsername()));
        if (optionalUser.isPresent()) {
            throw RegisterExceptions.userAlreadyExists();
        }

        try {
            // 2. 유저 정보 저장
            UserEntity user = UserEntity.builder()
                    .username(registerDTO.getUsername())
                    .password(passwordEncoder.encode(registerDTO.getPassword()))
                    .role(registerDTO.getRole())
                    .build();

            userRepository.save(user);

            // 3. 유저 프로필 저장
            UserProfileEntity userProfile = UserProfileEntity.builder()
                    .full_name(registerDTO.getFull_name())
                    .date_of_birth(registerDTO.getDate_of_birth())
                    .phone_number(registerDTO.getPhone_number())
                    .username(user)
                    .build();

            userProfileRepository.save(userProfile);

            return null;

        } catch (Exception e) {
            throw RegisterExceptions.registrationFailed();

    }

}
    public boolean isUsernameDuplicate(String username) {
        // username으로 UserEntity를 찾음
        UserEntity user = userRepository.findByUsername(username);

        // user가 null이면 중복되지 않음, 아니면 중복됨
        return user != null; // 중복이면 true, 아니면 false
    }


}