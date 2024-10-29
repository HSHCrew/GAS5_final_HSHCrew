package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "drug")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationEntity {

    @Id
    @Column(name = "item_seq", nullable = false, unique = true)
    private Integer medicationId; // 품목기준코드


    @Column(nullable = false)
    private String result_code; // 결과코드

    @Column(nullable = false)
    private String result_msg; // 결과메시지

    private Integer num_of_rows; // 한 페이지 결과 수

    private Integer page_no; // 페이지 번호

    private Integer total_count; // 전체 결과 수

    private String entp_name; // 업체명
    @Column(name = "item_name")
    private String medication_name; // 제품명


    @Lob
    @Column(name = "efcy_qesitm")
    private String medication_info; // 효능 문항

    @Lob
    private String use_method_qesitm; // 사용법 문항

    @Lob
    private String atpn_warn_qesitm; // 주의사항 경고 문항

    @Lob
    private String atpn_qesitm; // 주의사항 문항

    @Lob
    @Column(name = "intrc_qesitm")
    private String interaction_info; // 상호작용 문항

    @Lob
    private String se_qesitm; // 부작용 문항

    @Lob
    private String deposit_method_qesitm; // 보관법 문항

    private String open_de; // 공개일자

    private String update_de; // 수정일자

    @Lob
    private String item_image; // 약물 이미지 URL
    @CreatedDate
    private LocalDateTime drug_created_at;
    @LastModifiedDate
    private LocalDateTime drug_updated_at;

}
