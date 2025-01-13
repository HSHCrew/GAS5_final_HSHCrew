package org.zerock.Altari.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "qna_comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "qna_comment_parent_id")
    private Integer qnaCommentParentId;

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


