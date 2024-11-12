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
            MedicationEntity medicationEntity = MedicationEntity.builder()
                    .resultCode("00")
                    .resultMsg("성공")
                    .numOfRows(1)
                    .pageNo(1)
                    .totalCount(1)
                    .entpName("제약회사 A")
                    .medicationName("수면제 A")
                    .medicationId("641607171")
                    .medicationInfo("당뇨 치료에 효과적입니다.")
                    .useMethodQesitm("식사 전에 복용하세요.")
                    .atpnWarnQesitm("알레르기 반응이 있을 수 있습니다.")
                    .atpnQesitm("임신 중 사용을 피하십시오.")
                    .interactionInfo("다른 당뇨약과 함께 복용하지 마세요.")
                    .seQesitm("두통, 구토 등의 부작용이 있을 수 있습니다.")
                    .depositMethodQesitm("서늘하고 건조한 곳에 보관하세요.")
                    .openDe("2023-01-01")
                    .updateDe("2023-10-27")
                    .itemImage("http://example.com/image.jpg")
                    .build();

            // drugRepository에 drugEntity 저장
            medicationRepository.save(medicationEntity);
        }
    }

