package org.zerock.Altari.user.dto;

import lombok.*;
import org.zerock.Altari.user.entity.DiseaseEntity;
import org.zerock.Altari.user.entity.UserEntity;

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
