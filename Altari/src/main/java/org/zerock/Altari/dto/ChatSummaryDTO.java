package org.zerock.Altari.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatSummaryDTO {
    private int chat_summary_id;
    private UserProfileDTO user_profile_id;
    private String chat_date;
    private String summary_content;

}
