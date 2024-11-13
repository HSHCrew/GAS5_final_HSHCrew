package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String medicationId; // 품목기준코드


    @Column(nullable = false, name = "result_code")
    private String resultCode; // 결과코드

    @Column(nullable = false, name = "result_msg")
    private String resultMsg; // 결과메시지

    @Column(name = "num_of_rows")
    private Integer numOfRows; // 한 페이지 결과 수

    @Column(name = "page_no")
    private Integer pageNo; // 페이지 번호

    @Column(name = "total_count")
    private Integer totalCount; // 전체 결과 수

    @Column(name = "entp_name")
    private String entpName; // 업체명

    @Column(name = "item_name", unique = true)
    private String medicationName; // 제품명


    @Lob
    @Column(name = "efcy_qesitm")
    private String medicationInfo; // 효능 문항

    @Lob
    @Column(name = "use_method_qesitm")
    private String useMethodQesitm; // 사용법 문항

    @Lob
    @Column(name = "atpn_warn_qesitm")
    private String atpnWarnQesitm; // 주의사항 경고 문항

    @Lob
    @Column(name = "atpn_qesitm")
    private String atpnQesitm; // 주의사항 문항

    @Lob
    @Column(name = "intrc_qesitm")
    private String interactionInfo; // 상호작용 문항

    @Lob
    @Column(name = "se_qesitm")
    private String seQesitm; // 부작용 문항

    @Lob
    @Column(name = "deposit_method_qesitm")
    private String depositMethodQesitm; // 보관법 문항

    @Column(name = "open_de")
    private String openDe; // 공개일자

    @Column(name = "update_de")
    private String updateDe; // 수정일자

    @Lob
    @Column(name = "item_image")
    private String itemImage; // 약물 이미지 URL
    @CreatedDate
    private LocalDateTime drug_created_at;
    @LastModifiedDate
    private LocalDateTime drug_updated_at;


    public MedicationEntity(String medicationName) {
        this.medicationName = medicationName;
    }

}
