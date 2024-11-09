package org.zerock.Altari.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.repository.DiseaseRepository;

@SpringBootTest
public class DiseaseRepositoryTests {

    @Autowired
    private DiseaseRepository diseaseRepository;

    @Test
    public void testInsert() {
        // 테스트 데이터 추가

        for (int i = 1; i <= 10; i++) {
            DiseaseEntity diseaseEntity = DiseaseEntity.builder()
                    .diseaseId(i)
                    .diseaseName("고혈압")
                    .build();

            diseaseRepository.save(diseaseEntity);
        }
    }
}

//    @Test
//    public void testFindByDiseaseName() {
//        // 테스트: "당뇨"로 질병 검색
//        String diseaseName = "당뇨";
//        DiseaseEntity foundDisease = diseaseRepository.findByDiseaseName(diseaseName).orElse(null);
//
//        // 검증: 데이터가 잘 저장되었는지 확인
//        assertThat(foundDisease).isNotNull();
//        assertThat(foundDisease.getDisease_id()).isEqualTo(1);
//        assertThat(foundDisease.getDisease_name()).isEqualTo("당뇨");
//        assertThat(foundDisease.getDisease_info()).isEqualTo("위험");
//    }
//}
