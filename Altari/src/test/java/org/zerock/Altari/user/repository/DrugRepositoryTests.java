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
                    .result_code("00")
                    .result_msg("성공")
                    .num_of_rows(1)
                    .page_no(1)
                    .total_count(1)
                    .entp_name("제약회사 A")
                    .medication_name("수면제 A")
                    .medicationId(641607171)
                    .medication_info("당뇨 치료에 효과적입니다.")
                    .use_method_qesitm("식사 전에 복용하세요.")
                    .atpn_warn_qesitm("알레르기 반응이 있을 수 있습니다.")
                    .atpn_qesitm("임신 중 사용을 피하십시오.")
                    .interaction_info("다른 당뇨약과 함께 복용하지 마세요.")
                    .se_qesitm("두통, 구토 등의 부작용이 있을 수 있습니다.")
                    .deposit_method_qesitm("서늘하고 건조한 곳에 보관하세요.")
                    .open_de("2023-01-01")
                    .update_de("2023-10-27")
                    .item_image("http://example.com/image.jpg")
                    .build();

            // drugRepository에 drugEntity 저장
            medicationRepository.save(medicationEntity);
        }
    }

