package org.zerock.Altari.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HealthKeywordDTO {
    private int health_keyword_id;
    private String health_keyword;
    private LocalDateTime create_at;
    private LocalDateTime update_at;
}
