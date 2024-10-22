package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.ModicationEntity;
import org.zerock.Altari.entity.UserEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AllergyDTO {
    private int allergy_id;
    private UserEntity user_profile_id;
    private ModicationEntity modication_id;
    private String food_name;
}
