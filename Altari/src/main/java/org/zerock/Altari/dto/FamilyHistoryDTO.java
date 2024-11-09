package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FamilyHistoryDTO {
    private Integer familyHistoryId;
    private DiseaseEntity disease;
    private UserProfileEntity userProfile;
    private String familyRelation;
    private LocalDateTime family_history_created_at;
    private LocalDateTime family_history_updated_at;

}
