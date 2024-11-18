package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_interaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationInteractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_interaction_id")
    private Integer medicationInteractionId; // PK (자동 생성)

    @Column(name = "medication_code")
    private String medicationCode; // 의약품 코드

    @Column(name = "medication_name", columnDefinition = "TEXT")
    private String medicationName; // 약물명

    @Column(name = "ingredient1")
    private String ingredient1; // 성분1 (nullable)

    @Column(name = "ingredient2")
    private String ingredient2; // 성분2 (nullable)

    @Column(name = "clinical_effect", columnDefinition = "TEXT")
    private String clinicalEffect; // 임상효과 (nullable)

    @Column(name = "mechanism", columnDefinition = "TEXT")
    private String mechanism; // 기전 (nullable)

    @Column(name = "treatment", columnDefinition = "TEXT")
    private String treatment; // 처치 (nullable)

    @ManyToOne
    @JoinColumn(name = "medication_id")
    private MedicationEntity medication;

    @Column(name = "medication_interaction_created_at")
    @CreatedDate
    private LocalDateTime medicationInteractionCreatedAt; // 생성 일시

    @Column(name = "medication_interaction_updated_at")
    @LastModifiedDate
    private LocalDateTime medicationInteractionUpdatedAt; // 수정 일시
}
