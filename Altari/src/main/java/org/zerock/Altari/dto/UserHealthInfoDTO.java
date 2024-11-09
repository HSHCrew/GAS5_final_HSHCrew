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
    private List<Integer> diseaseId;
    private List<Integer> pastDiseaseId;
    private List<Integer> familyDiseaseId;
    private List<String> allergyMedicationId;


}
