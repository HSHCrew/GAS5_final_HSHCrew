package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;

@Data
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PrescriptionDrugDTO {

    private Integer prescription_drug_id;
    private Integer prescription_id;
    private MedicationEntity medicationId;
    private String one_dose;
    private int dailyDosesNumber;
    private int taken_dosing_days;
    private int total_dosing_days;
    private String medication_direction;
    private int total_dosage;
    private int taken_dosage;
}
