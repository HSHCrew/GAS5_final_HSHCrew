package org.zerock.Altari.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_food_interaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationFoodInteractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_food_interaction_id")
    private Integer MedicationFoodInteractionId; // PK (자동 생성)

    @Column(name = "medication_code")
    private String medicationCode; // 의약품 코드

    @Column(name = "medication_name", columnDefinition = "TEXT")
    private String medicationName; // 약물명 (한글)

    @Column(name = "medication_ingredient", columnDefinition = "TEXT")
    private String medicationIngredient; // 성분 (nullable)

    @Column(name = "atpn_qesitm", columnDefinition = "TEXT")
    private String atpnQesitm; // 주의사항 (nullable)

    @ManyToOne
    @JoinColumn(name = "medication_id")
    private MedicationEntity medication;

    @Column(name = "medication_food_interaction_created_at")
    @CreatedDate
    private LocalDateTime medicationFoodInteractionCreatedAt;

    @Column(name = "medication_food_interaction_updated_at")
    @LastModifiedDate
    private LocalDateTime medicationFoodInteractionUpdatedAt;
}
