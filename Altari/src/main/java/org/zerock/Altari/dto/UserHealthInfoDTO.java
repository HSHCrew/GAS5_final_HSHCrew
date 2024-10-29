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
    private List<Integer> disease_id;
    @JsonProperty("past_disease_id")
    private List<Integer> past_disease_id;
    @JsonProperty("family_disease_id")
    private List<Integer> family_disease_id;
    @JsonProperty("allergy_medication_id")
    private List<Integer> allergy_medication_id;


}
