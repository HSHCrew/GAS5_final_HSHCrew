package org.zerock.Altari.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.zerock.Altari.entity.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserHealthInfoDTO {
    private List<DiseaseEntity> diseases;
    private List<DiseaseEntity> pastDiseases;
    private List<DiseaseEntity> familyDiseases;
    private List<MedicationEntity> allergyMedications;

}
