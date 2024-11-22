package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "disease")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DiseaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer diseaseId;

    @Column(name = "disease_name", columnDefinition = "TEXT")
    private String diseaseName;  // 질환명

    @Column(name = "classification_code")
    private String classificationCode;  // 상병코드

        @Column(name = "classification", columnDefinition = "TEXT")
    private String classification;  // 분류

    @Column(name = "disease_definition", columnDefinition = "TEXT")
    private String diseaseDefinition;  // 정의

    @Column(name = "cause", columnDefinition = "TEXT")
    private String cause;  // 원인

    @Column(name = "symptom", columnDefinition = "TEXT")
    private String symptom;  // 증상

    @Column(name = "treatment", columnDefinition = "TEXT")
    private String treatment;  // 치료

    @Column(name = "etc_info", columnDefinition = "TEXT")
    private String etcInfo;  // 기타

    @Column(name = "attention", columnDefinition = "TEXT")
    private String attention;  // 복약 주의사항

    @Column(name = "medication_attention", columnDefinition = "TEXT")
    private String medicationAttention;  // 복약 주의사항

    @Column(name = "life_attention", columnDefinition = "TEXT")
    private String lifeAttention;  // 일상생활 주의점

    @CreatedDate
    private LocalDateTime diseaseCreatedAt;

    @LastModifiedDate
    private LocalDateTime diseaseUpdatedAt;

    @Column(name = "is_hereditary")
    private Boolean isHereditary;  // 유전 여부

    public DiseaseEntity(int diseaseId) {
        this.diseaseId = diseaseId;
    }

}
