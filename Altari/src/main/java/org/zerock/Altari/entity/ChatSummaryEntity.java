package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_summary")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int chat_summary_id;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity user_profile_id;

    private String chat_date;
    private String summary_content;

    @CreatedDate
    private LocalDateTime chat_summary_created_at;
    @LastModifiedDate
    private LocalDateTime chat_summary_updated_at;

}