package org.zerock.Altari.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HealthKeywordDTO {
    private Integer health_keyword_id;
    private String health_keyword;
    private LocalDateTime health_keyword_created_at;
    private LocalDateTime health_keyword_updated_at;
}
