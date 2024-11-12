package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.DiseaseEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDiseaseDTO {

    private Integer userDiseaseId;
    private UserProfileDTO userProfile;
    private DiseaseEntity disease;

}
