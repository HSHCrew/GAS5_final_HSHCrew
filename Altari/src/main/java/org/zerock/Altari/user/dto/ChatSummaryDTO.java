package org.zerock.Altari.user.dto;

import lombok.*;
import org.zerock.Altari.user.entity.UserEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatSummaryDTO {
    private int chat_summary_id;
    private UserEntity user_profile_id;
    private String chat_date;
    private String summary_content;

}
