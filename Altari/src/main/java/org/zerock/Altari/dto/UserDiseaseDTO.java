package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.UserEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDiseaseDTO {

    private int user_disease_id;
    private UserEntity user_profile_id;
    private DiseaseEntity disease_id;

}
