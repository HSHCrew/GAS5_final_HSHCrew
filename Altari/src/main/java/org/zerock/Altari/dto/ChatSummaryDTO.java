package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatSummaryDTO {

    private Integer chatSummaryId;
    private UserProfileEntity userProfile;
    private String chatDate;
    private String summaryContent;
    private LocalDateTime chat_summary_created_at;
    private LocalDateTime chat_summary_updated_at;

}
