package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private Integer prescription_drug_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_prescription_id", nullable = false)
    private UserPrescriptionEntity prescriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_seq", nullable = false)
    private MedicationEntity MedicationId;

    @Column(nullable = false)
    private String one_dose;

    @Column(nullable = false)
    private int dailyDosesNumber;

    private int taken_dosing_days;

    @Column(nullable = false)
    private int total_dosing_days;

    private int total_dosage;

    private int taken_dosage;

    private String medication_direction;

    @Column(name = "today_taken_count")
    private int todayTakenCount; // 오늘 복용한 횟수

    @Column(name = "last_taken_date")
    private LocalDate lastTakenDate; // 또는 Timestamp 타입으로 설정 가능

    public boolean canIncreaseTakenDosage() {
        // 오늘 날짜를 가져옵니다.
        LocalDate today = LocalDate.now();

        // 마지막 복용일이 null인 경우, 또는 오늘 복용하지 않은 경우에만 증가 가능
        if (lastTakenDate == null || !lastTakenDate.isEqual(today)) {
            return true; // 오늘 복용을 하지 않았으므로 증가 가능
        }
        return false; // 오늘 이미 복용했으므로 증가 불가
    }


}
