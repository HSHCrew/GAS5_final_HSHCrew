package org.zerock.Altari.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.repository.MedicationRepository;

@SpringBootTest
public class DrugRepositoryTests {
    @Autowired
    private MedicationRepository medicationRepository;

    @Test
    public void testInsert() {
        // 테스트 데이터 추가
        // Medication 데이터 준비 (약 데이터 예시)
        MedicationEntity medication = MedicationEntity.builder()
                .medicationName("헤브론에프정")
                .build();
        medicationRepository.save(medication);
        }
    }

