package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.UserProfileEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatSummaryDTO {
    private int chat_summary_id;
    private UserProfileEntity user_profile_id;
    private String chat_date;
    private String summary_content;

}
