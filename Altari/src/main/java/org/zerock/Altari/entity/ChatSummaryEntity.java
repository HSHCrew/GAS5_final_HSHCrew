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
    @Column(name = "chat_summary_id")
    private Integer chatSummaryId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;

    @Column(name = "chat_date")
    private LocalDateTime chatDate;

    @Column(name = "summary_content", columnDefinition = "TEXT")
    private String summaryContent;

    @CreatedDate
    private LocalDateTime chat_summary_created_at;

    @LastModifiedDate
    private LocalDateTime chat_summary_updated_at;

}