package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.HealthKeywordEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KeywordRegistrationDTO {
    private Integer keyword_registration_id;
    private UserProfileEntity user_profile_id;
    private HealthKeywordEntity health_keyword_id;
    private LocalDateTime keyword_registration_created_at;
    private LocalDateTime keyword_registration_updated_at;
}
