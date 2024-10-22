package org.zerock.Altari.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PrescriptionDTO {
    private int prescription_id;
    private UserProfileDTO user_profile_id;
    private String prescription_info;
    private String ai_summary;
    private String adherence_score;
}
