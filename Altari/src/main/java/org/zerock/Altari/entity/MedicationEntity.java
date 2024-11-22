package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Entity
@Table(name = "medication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_id")
    private Integer medicationId; //

    @Column(name = "medication_code")
    private String itemSeq; // 대표코드

    @Column(name = "medication_item_standard_code")
    private String medicaitonItemStandardCode; // 품목기준코드

    @Column(name = "medication_item_dur", columnDefinition = "TEXT")
    private String medicationItemDur; // 품목dur

    @Column(name = "medication_brand_name", columnDefinition = "TEXT")
    private String entpName; // 업체명

    @Column(name = "medication_name", unique = true, columnDefinition = "TEXT")
    private String medicationName; // 제품명

    @Column(name = "ingredient_code", columnDefinition = "TEXT")
    private String ingredientCode; // 주성분 코드

    @Column(name = "ingredient", columnDefinition = "TEXT")
    private String ingredient; // 성분/함량

    @Column(name = "additives", columnDefinition = "TEXT")
    private String additives; // 첨가제

    @Lob
    @Column(name = "medication_efficacy_info", columnDefinition = "TEXT")
    private String medicationInfo; // 효능 문항

    @Lob
    @Column(name = "medication_use_info", columnDefinition = "TEXT")
    private String useMethodQesitm; // 사용법 문항

    @Lob
    @Column(name = "medication_caution_info", columnDefinition = "TEXT")
    private String atpnQesitm; // 주의사항 문항

    @Lob
    @Column(name = "medication_caution_warning_info", columnDefinition = "TEXT")
    private String medicationCautionWarningInfo; // 주의사항 경고 문항

    @Lob
    @Column(name = "medication_interaction_info", columnDefinition = "TEXT")
    private String interactionInfo; // 상호작용 문항

    @Lob
    @Column(name = "taking_info", columnDefinition = "TEXT")
    private String takingInfo; // 복약정보 문항

    @Lob
    @Column(name = "medication_se_info", columnDefinition = "TEXT")
    private String seQesitm; // 부작용 문항

    @Lob
    @Column(name = "medication_storage_method_info", columnDefinition = "TEXT")
    private String depositMethodQesitm; // 보관법 문항

    @Column(name = "open_de")
    private String openDe; // 공개일자

    @Column(name = "update_de")
    private String updateDe; // 수정일자

    @Column(name = "medication_image", columnDefinition = "TEXT")
    private String itemImage; // 약물 이미지 URL

    @CreatedDate
    private LocalDateTime medication_created_at;
    @LastModifiedDate
    private LocalDateTime medication_updated_at;


    public MedicationEntity(Integer medicationId) {
        this.medicationId = medicationId;
    }

    @Override
    public String toString() {
        return "MedicationEntity{" +
                "id=" + medicationId +
                ", name='" + medicationName + '\'' +
                ", dosage='" + medicationInfo + '\'' +
                // 다른 속성들 추가
                '}';
    }


}
