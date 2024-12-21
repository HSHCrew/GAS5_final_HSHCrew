package org.zerock.Altari.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.zerock.Altari.entity.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserHealthInfoDTO {
    private Set<String> currentDiseases;    // 현재 질병
    private Set<String> pastDiseases;       // 과거 질병
    private Set<String> familyHistories;    // 가족력
    private Set<String> allergies;          // 알레르기
}

