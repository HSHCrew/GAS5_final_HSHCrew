package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.MedicationEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewsCurationDTO {

    private Integer news_curation_id;
    private MedicationEntity medication_id;
    private String news_content;
    private LocalDateTime news_curation_created_at;
    private LocalDateTime news_curation_updated_at;
}
