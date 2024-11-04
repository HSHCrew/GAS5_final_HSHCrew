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

            // 3. 유저 프로필 저장
            UserProfileEntity userProfile = UserProfileEntity.builder()
                    .full_name(registerDTO.getFull_name())
                    .date_of_birth(registerDTO.getDate_of_birth())
                    .phone_number(registerDTO.getPhone_number())
                    .username(user)
                    .build();

            userProfileRepository.save(userProfile);

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

    public void deleteUser(String username) {
        UserEntity userEntity = userRepository.findByUsername(username);
        UserProfileEntity userProfile = userProfileRepository.findByUsername(userEntity);
        if (userEntity == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        if (userProfile != null) {
            userProfileRepository.delete(userProfile);
        }

        userRepository.delete(userEntity);

    }


}