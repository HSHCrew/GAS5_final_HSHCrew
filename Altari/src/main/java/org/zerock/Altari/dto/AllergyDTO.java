package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.MedicationEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AllergyDTO {
    private int allergy_id;
    private UserProfileDTO user_profile_id;
    private MedicationEntity medication_id;
    private String food_name;
}
