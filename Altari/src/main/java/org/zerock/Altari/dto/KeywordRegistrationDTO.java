package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KeywordRegistrationDTO {

    private Integer keywordRegistrationId;
    private UserProfileEntity userProfile;
    private LocalDateTime keyword_registration_created_at;
    private LocalDateTime keyword_registration_updated_at;
}
