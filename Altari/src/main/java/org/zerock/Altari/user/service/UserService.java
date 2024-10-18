package org.zerock.Altari.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.user.dto.UserDTO;
import org.zerock.Altari.user.entity.UserEntity;
import org.zerock.Altari.user.repository.UserRepository;
import org.zerock.Altari.user.exception.UserExceptions;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}

