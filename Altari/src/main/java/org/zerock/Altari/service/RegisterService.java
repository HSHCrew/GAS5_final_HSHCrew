package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.RegisterDTO;
import org.zerock.Altari.dto.UserDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.exception.RegisterExceptions;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.time.LocalTime;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileService userProfileService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserMedicationTimeRepository userMedicationTimeRepository;

    @Transactional
    public UserEntity join(RegisterDTO registerDTO) {

        try {
            // 2. 유저 정보 저장
            UserEntity user = UserEntity.builder()
                    .username(registerDTO.getUsername())
                    .password(passwordEncoder.encode(registerDTO.getPassword()))
                    .role(registerDTO.getRole())
                    .build();

            userRepository.save(user);

            String rawPhoneNumber = registerDTO.getPhoneNumber();
            String formattedPhoneNumber = userProfileService.formatPhoneNumber(rawPhoneNumber);

            // 3. 유저 프로필 저장
            UserProfileEntity userProfile = UserProfileEntity.builder()
                    .fullName(registerDTO.getFullName())
                    .dateOfBirth(registerDTO.getDateOfBirth())
                    .phoneNumber(formattedPhoneNumber)
                    .username(user)
                    .morningMedicationTime(LocalTime.parse("10:00"))
                    .lunchMedicationTime(LocalTime.parse("14:00"))
                    .dinnerMedicationTime(LocalTime.parse("19:00"))
                    .build();

            userProfileRepository.save(userProfile);

            UserMedicationTimeEntity userMedicationTime = UserMedicationTimeEntity.builder()
                    .onMorningMedicationAlarm(true)
                    .onLunchMedicationTimeAlarm(true)
                    .onDinnerMedicationTimeAlarm(true)
                    .onNightMedicationTimeAlarm(true)
                    .userProfile(userProfile)
                    .build();

            userMedicationTimeRepository.save(userMedicationTime);

            return user;

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

    @Transactional
    public void deleteUser(String username) {
        // 외래 키 제약을 비활성화
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        try {
            UserEntity userEntity = userRepository.findByUsername(username);
            if (userEntity == null) {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }

            UserProfileEntity userProfile = userProfileRepository.findByUsername(userEntity);
            if (userProfile != null) {
                // 관련 데이터 삭제
                userProfileRepository.delete(userProfile);
            }

            userRepository.delete(userEntity);
        } finally {
            // 외래 키 제약을 다시 활성화
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }



}