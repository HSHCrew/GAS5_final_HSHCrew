package org.zerock.Altari.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "expert_feedback")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ExpertFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expert_feedback_id")
    private Integer expertFeedbackId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "chat_summary_id")
    private ChatSummaryEntity ChatSummary;

    @Column(name = "expert_feedback_content", columnDefinition = "TEXT")
    private String expertFeedbackContent; // DUR일련번호

    @CreatedDate
    private LocalDateTime expert_feedback_created_at;
}
