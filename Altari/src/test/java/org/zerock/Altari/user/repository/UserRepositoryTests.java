package org.zerock.Altari.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testInsert() {

        for (int i = 1; i <= 10; i++) {

            UserEntity userEntity = UserEntity.builder()
                    .username("user" + i)
                    .password(passwordEncoder.encode("1111"))
                    .create_at(LocalDateTime.now())
                    .update_at(LocalDateTime.now())
                    .role( i <= 80 ? "USER":"ADMIN")
                    .build();

            userRepository.save(userEntity);
        }
    }



    @Test
    @Transactional
    @Commit
    public void testRead() {

        String username = "user1";

        Optional<UserEntity> result = userRepository.findById(username);

        UserEntity userEntity = result.orElseThrow(UserExceptions.NOT_FOUND::get);

        userEntity.changePassword(passwordEncoder.encode("2222"));
    }

    @Test
    @Transactional
    @Commit
    public void testUpdate() {
        String username = "user1";

        Optional<UserEntity> result = userRepository.findById(username);

        UserEntity userEntity = result.orElseThrow(UserExceptions.NOT_FOUND::get);

        userEntity.changePassword(passwordEncoder.encode("2222"));
    }

     @Commit
     @Test
     @Transactional
     public void testDelete() {
         String username = "user1";
         Optional<UserEntity> result = userRepository.findById(username);
         UserEntity userEntity = result.orElseThrow(UserExceptions.NOT_FOUND::get);
         userRepository.delete(userEntity);
     }
}

