package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Integer MedicationId; //

    @Column(name = "medication_code", nullable = false, unique = true)
    private String itemSeq; // 품목기준코드

    @Column(name = "medication_promo_code", nullable = false, unique = true)
    private String medicaitonPromoCode; // 대표코드

    @Column(name = "medication_item_dur")
    private String medicationItemDur; // 품목dur

    @Column(name = "medication_brand_name")
    private String entpName; // 업체명

    @Column(name = "medication_name", unique = true)
    private String medicationName; // 제품명

    @Column(name = "ingredient_code")
    private String ingredientCode; // 주성분 코드

    @Column(name = "ingredient")
    private String ingredient; // 성분/함량

    @Column(name = "additives")
    private String additives; // 성분/함량

    @Lob
    @Column(name = "medication_efficacy_info", columnDefinition = "TEXT")
    private String medicationInfo; // 효능 문항

    @Lob
    @Column(name = "medication_use_info")
    private String useMethodQesitm; // 사용법 문항

    @Lob
    @Column(name = "medication_caution_info", columnDefinition = "TEXT")
    private String atpnQesitm; // 주의사항 문항

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

    @Lob
    @Column(name = "medication_image", columnDefinition = "TEXT")
    private String itemImage; // 약물 이미지 URL
    @CreatedDate
    private LocalDateTime medication_created_at;
    @LastModifiedDate
    private LocalDateTime medication_updated_at;


    public MedicationEntity(Integer medicationId) {
        this.MedicationId = medicationId;
    }

}
