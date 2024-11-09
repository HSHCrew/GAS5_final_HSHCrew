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
    @JsonProperty("disease_id")
    private List<Integer> diseaseId;
    @JsonProperty("past_disease_id")
    private List<Integer> pastDiseaseId;
    @JsonProperty("family_disease_id")
    private List<Integer> familyDiseaseId;
    @JsonProperty("allergy_medication_id")
    private List<String> allergyMedicationId;


}
