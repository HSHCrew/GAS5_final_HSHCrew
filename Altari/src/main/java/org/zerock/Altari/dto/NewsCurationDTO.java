package org.zerock.Altari.dto;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.MedicationEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewsCurationDTO {

    private Integer newsCurationId;
    private MedicationEntity medication;
    private String newsContent;
    private LocalDateTime news_curation_created_at;
    private LocalDateTime news_curation_updated_at;
}
