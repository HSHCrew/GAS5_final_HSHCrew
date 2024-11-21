package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_medication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
public class UserMedicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_medication_id")
    private Integer UserMedicationId;

    @ManyToOne
    @JoinColumn(name = "user_prescription_id")
    private UserPrescriptionEntity prescriptionId;

    @ManyToOne
    @JoinColumn(name = "medication_id", referencedColumnName = "medication_id")
    private MedicationEntity medication;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;

    @Column(nullable = false, name = "one_dose")
    private String oneDose;

    @Column(nullable = false)
    private int dailyDosesNumber;

    @Column(name = "taken_dosing_days")
    private int takenDosingDays;

    @Column(nullable = false, name = "total_dosing_days")
    private int totalDosingDays;

    @Column(name = "total_dosage")
    private int totalDosage;

    @Column(name = "taken_dosage")
    private int takenDosage;

    @Column(name = "medication_direction", columnDefinition = "TEXT")
    private String medicationDirection;

    @Column(name = "today_taken_count")
    private int todayTakenCount; // 오늘 복용한 횟수

    @Column(name = "last_taken_date")
    private LocalDate lastTakenDate; // 또는 Timestamp 타입으로 설정 가능

    @CreatedDate
    private LocalDateTime user_medication_created_at;
    @LastModifiedDate
    private LocalDateTime user_medication_updated_at;



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
