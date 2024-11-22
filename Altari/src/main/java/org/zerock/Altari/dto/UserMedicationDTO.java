package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.MedicationEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserMedicationDTO {

    private Integer prescriptionDrugId;
    private Integer prescriptionId;
    private MedicationEntity Medication;
    private String oneDose;
    private int dailyDosesNumber;
    private int takenDosingDays;
    private int totalDosingDays;
    private int totalDosage;
    private int takenDosage;
    private String medicationDirection;
    private int todayTakenCount; // 오늘 복용한 횟수
    private LocalDate lastTakenDate;
    private LocalDateTime prescription_drug_created_at;
    private LocalDateTime prescription_drug_updated_at;
}
