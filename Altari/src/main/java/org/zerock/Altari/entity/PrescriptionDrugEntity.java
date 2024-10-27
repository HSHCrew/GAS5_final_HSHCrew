package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescription_drug")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDrugEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int prescription_drug_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private UserPrescriptionEntity prescription_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_seq", nullable = false)
    private DrugEntity item_seq;

    @Column(nullable = false)
    private String one_dose;

    @Column(nullable = false)
    private String dailyDosesNumber;

    @Column(nullable = false)
    private String total_dosing_days;

    private String medication_direction;
}
