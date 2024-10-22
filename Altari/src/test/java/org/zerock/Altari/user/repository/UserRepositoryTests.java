package org.zerock.Altari.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.repository.DiseaseRepository;
import org.zerock.Altari.repository.MedicationRepository;
import org.zerock.Altari.repository.UserRepository;

import java.time.LocalDateTime;

@SpringBootTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DiseaseRepository diseaseRepository;
    @Autowired
    private MedicationRepository medicationRepository;

    //
    @Test
    public void testInsert() {
        // 테스트 데이터 추가
        DiseaseEntity diseaseEntity = DiseaseEntity.builder()
                .disease_id(1)
                .disease_name("당뇨")
                .disease_info("위험")
                .build();

        diseaseRepository.save(diseaseEntity);
    }

    @Test
    public void testInsertMedication() {
        // 테스트 데이터 추가
        MedicationEntity medicationEntity = MedicationEntity.builder()
                .medication_id(1)
                .medication_name("타이레놀")
                .medication_info("감기약")
                .interaction_info("사과")
                .build();

        medicationRepository.save(medicationEntity);


    }

//}
    @Test
    public void testInsertUser() {

        for (int i = 1; i <= 10; i++) {

            UserEntity userEntity = UserEntity.builder()
                    .username("user" + i)
                    .password(passwordEncoder.encode("1111"))
                    .user_created_at(LocalDateTime.now())
                    .user_updated_at(LocalDateTime.now())
                    .role(i <= 80 ? "USER" : "ADMIN")
                    .build();

            userRepository.save(userEntity);
        }
    }
}



//    @Test
//    @Transactional
//    @Commit
//    public void testRead() {
//
//        String username = "user1";
//
//        Optional<UserEntity> result = userRepository.findById(username);
//
//        UserEntity userEntity = result.orElseThrow(UserExceptions.NOT_FOUND::get);
//
//        userEntity.changePassword(passwordEncoder.encode("2222"));
//    }
//
//    @Test
//    @Transactional
//    @Commit
//    public void testUpdate() {
//        String username = "user1";
//
//        Optional<UserEntity> result = userRepository.findById(username);
//
//        UserEntity userEntity = result.orElseThrow(UserExceptions.NOT_FOUND::get);
//
//        userEntity.changePassword(passwordEncoder.encode("2222"));
//    }
//
//     @Commit
//     @Test
//     @Transactional
//     public void testDelete() {
//         String username = "user1";
//         Optional<UserEntity> result = userRepository.findById(username);
//         UserEntity userEntity = result.orElseThrow(UserExceptions.NOT_FOUND::get);
//         userRepository.delete(userEntity);
//     }
//}
//
