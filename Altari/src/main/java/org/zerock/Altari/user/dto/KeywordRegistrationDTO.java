package org.zerock.Altari.user.dto;

import lombok.*;
import org.zerock.Altari.user.entity.HealthKeywordEntity;
import org.zerock.Altari.user.entity.UserEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KeywordRegistrationDTO {
    private int keyword_registration_id;
    private UserEntity user_profile_id;
    private HealthKeywordEntity health_keyword_id;
    private LocalDateTime create_at;
    private LocalDateTime update_at;
}
