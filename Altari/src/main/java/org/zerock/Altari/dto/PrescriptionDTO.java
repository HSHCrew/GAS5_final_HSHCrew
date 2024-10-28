package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.UserProfileEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PrescriptionDTO {
    private Integer prescription_id;
    private UserProfileEntity user_profile_id;
    private String prescription_info;
    private String ai_summary;
    private String adherence_score;
}
