package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.DiseaseEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserPastDiseaseDTO {
    private Integer userPastDiseaseId;
    private UserProfileDTO userProfile;
    private DiseaseEntity disease;
}
