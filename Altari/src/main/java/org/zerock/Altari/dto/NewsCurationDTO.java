package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.HealthKeywordEntity;
import org.zerock.Altari.entity.ModicationEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewsCurationDTO {

    private int news_curation_id;
    private HealthKeywordEntity health_keyword_id;
    private ModicationEntity medication_id;
    private String news_content;
    private LocalDateTime user_create_at;
    private LocalDateTime user_update_at;
}
