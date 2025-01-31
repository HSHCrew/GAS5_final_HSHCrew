package org.zerock.Altari.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna_comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class QnaCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_comment_id")
    private Integer qnaCommentId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "qna_post_id")
    private QnaPostEntity qnaPost;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "qna_comment_group_id")
    private Integer qnaCommentGroupId;

    @Column(name = "qna_comment_group_order")
    private Integer qnaCommentGroupOrder;

    @Column(name = "qna_comment_depth")
    private Integer qnaCommentDepth;

    @Column(name = "qna_comment_content", columnDefinition = "TEXT")
    private String qnaCommentContent;

    @Column(name = "qna_comment_likes")
    private Integer qnaCommentLikes;

    @Column(name = "qna_comment_created_at")
    @CreatedDate
    private LocalDateTime qnaCommentCreatedAt;

    @Column(name = "qna_comment_updated_at")
    @LastModifiedDate
    private LocalDateTime qnaCommentUpdatedAt;
}


