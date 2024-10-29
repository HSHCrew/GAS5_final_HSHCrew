package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.UserProfileEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PastDiseaseDTO {
    private int past_disease_id;
    private UserProfileEntity user_profile_id;
    private DiseaseEntity disease_id;
}
