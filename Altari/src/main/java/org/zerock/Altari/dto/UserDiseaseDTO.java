package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.DiseaseEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDiseaseDTO {

    private Integer user_disease_id;
    private UserProfileDTO user_profile_id;
    private DiseaseEntity disease_id;

}
