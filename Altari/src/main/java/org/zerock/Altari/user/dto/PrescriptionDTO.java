package org.zerock.Altari.user.dto;

import lombok.*;
import org.zerock.Altari.user.entity.UserEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PrescriptionDTO {
    private int prescription_id;
    private UserEntity user_profile_id;
    private String prescription_info;
    private String ai_summary;
    private String adherence_score;
}
