package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.UserMedicationEntity;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TimedMedicationDTO {

    private List<MedicationNameImageDTO> MorningMedications;
    private List<MedicationNameImageDTO> LunchMedications;
    private List<MedicationNameImageDTO> DinnerMedications;
    private List<MedicationNameImageDTO> NightMedications;
}
