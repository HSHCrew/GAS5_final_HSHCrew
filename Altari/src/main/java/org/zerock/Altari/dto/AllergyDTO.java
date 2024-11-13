package org.zerock.Altari.dto;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AllergyDTO {
    private Integer allergyId;
    private UserProfileEntity userProfile;
    private MedicationEntity medicationName;
    private LocalDateTime allergy_created_at;
    private LocalDateTime allergy_updated_at;
}
