package org.zerock.Altari.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_success_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationSuccessRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_success_rate_id")
    private Integer medicationSuccessRateId;
    @Column(name = "current_dosage_count")
    private Integer currentDosageCount;
    @Column(name = "total_dosage_count")
    private Integer totalDosageCount;
    @CreatedDate
    private LocalDateTime medication_success_rate_created_at;
    @LastModifiedDate
    private LocalDateTime medication_success_rate_updated_at;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_prescription_id", nullable = false)
    private UserPrescriptionEntity prescriptionId;




}
