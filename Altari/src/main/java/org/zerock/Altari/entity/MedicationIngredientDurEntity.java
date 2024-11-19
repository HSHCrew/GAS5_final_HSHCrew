package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;


@Entity
@Table(name = "medication_ingredient_dur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationIngredientDurEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_ingredient_dur_id")
    private Integer medicationIngredientDurId;

    @ManyToOne
    @JoinColumn(name = "medication_id")
    private MedicationEntity medication;

    @Column(name = "ingredient_dur", columnDefinition = "TEXT")
    private String ingredientDur; // DUR일련번호

    @Column(name = "dur_serial_number")
    private String durSerialNumber; // DUR일련번호

    @Column(name = "dur_type",  columnDefinition = "TEXT")
    private String durType; // DUR유형

    @Column(name = "single_compound_code",columnDefinition = "TEXT")
    private String singleCompoundCode; // 단일복합구분코드

    @Column(name = "dur_ingredient_code")
    private String durIngredientCode; // DUR성분코드

    @Column(name = "dur_ingredient_name_eng", columnDefinition = "TEXT")
    private String durIngredientNameEng; // DUR성분명영문

    @Column(name = "dur_ingredient_name", columnDefinition = "TEXT")
    private String durIngredientName; // DUR성분명

    @Column(name = "compound", columnDefinition = "TEXT")
    private String compound; // 복합제

    @Column(name = "related_ingredient", columnDefinition = "TEXT")
    private String relatedIngredient; // 관계성분

    @Column(name = "efficacy_classification_code")
    private String efficacyClassificationCode; // 약효분류코드

    @Column(name = "efficacy_group", columnDefinition = "TEXT")
    private String efficacyGroup; // 효능군

    @Column(name = "announcement_date", columnDefinition = "TEXT")
    private String announcementDate; // 고시일자

    @Column(name = "prohibition_content", columnDefinition = "TEXT")
    private String prohibitionContent; // 금기내용

    @Column(name = "dosage_form", columnDefinition = "TEXT")
    private String dosageForm; // 제형

    @Column(name = "age_criteria", columnDefinition = "TEXT")
    private String ageCriteria; // 연령기준

    @Column(name = "max_administration_period", columnDefinition = "TEXT")
    private String maxAdministrationPeriod; // 최대투여기간

    @Column(name = "max_daily_dose", columnDefinition = "TEXT")
    private String maxDailyDose; // 1일최대용량

    @Column(name = "grade")
    private String grade; // 등급

    @Column(name = "combination_prohibition_single_compound_code")
    private String combinationProhibitionSingleCompoundCode; // 병용금기단일복합구분코드

    @Column(name = "combination_prohibition_dur_code")
    private String combinationProhibitionDurCode; // 병용금기DUR성분코드

    @Column(name = "combination_prohibition_dur_name", columnDefinition = "TEXT")
    private String combinationProhibitionDurName; // 병용금기DUR성분명

    @Column(name = "combination_prohibition_dur_name_eng", columnDefinition = "TEXT")
    private String combinationProhibitionDurNameEng; // 병용금기DUR성분명영문

    @Column(name = "combination_prohibition_compound", columnDefinition = "TEXT")
    private String combinationProhibitionCompound; // 병용금기복합제

    @Column(name = "combination_prohibition_related_ingredient", columnDefinition = "TEXT")
    private String combinationProhibitionRelatedIngredient; // 병용금기관계성분

    @Column(name = "combination_prohibition_efficacy_classification", columnDefinition = "TEXT")
    private String combinationProhibitionEfficacyClassification; // 병용금기약효분류

    @Column(name = "remarks")
    private String remarks; // 비고

    @Column(name = "status")
    private String status; // 상태

    @Column(name = "series_name")
    private String seriesName; // 계열명

    @Column(name = "medication_ingredient_dur_created_at")
    @CreatedDate
    private LocalDateTime medicationIngredientDurCreatedAt;

    @Column(name = "medication_ingredient_dur_updated_at")
    @LastModifiedDate
    private LocalDateTime medicationIngredientDurUpdatedAt;
}