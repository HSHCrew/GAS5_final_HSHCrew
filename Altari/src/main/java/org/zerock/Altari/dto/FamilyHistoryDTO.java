package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.DiseaseEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FamilyHistoryDTO {
    private int family_history_id;
    private DiseaseEntity disease_id;
    private UserProfileDTO user_profile_id;
    private String family_relation;
}
