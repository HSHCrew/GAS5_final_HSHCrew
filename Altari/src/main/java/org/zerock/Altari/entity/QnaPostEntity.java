package org.zerock.Altari.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class QnaPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_post_id")
    private Integer qnaPostId;

    @Column(name = "qna_post_group_id")
    private Integer qnaPostGroupId;

    @Column(name = "qna_post_group_order")
    private Integer qnaPostGroupOrder;

    @Column(name = "qna_post_depth")
    private Integer qnaPostDepth;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "qna_post_title", columnDefinition = "TEXT")
    private String qnaPostTitle;

    @Column(name = "qna_post_content", columnDefinition = "TEXT")
    private String qnaPostContent;

    @ManyToOne
    @JoinColumn(name = "qna_post_category_id")
    private QnaPostCategoryEntity qnaPostCategory;

    @Column(name = "qna_post_likes")
    private Integer qnaPostLikes;

    @Column(name = "qna_post_view_count")
    private Integer qnaPostViewCount;

    @Column(name = "qna_post_created_at")
    @CreatedDate
    private LocalDateTime qnaPostCreatedAt;

    @Column(name = "qna_post_updated_at")
    @LastModifiedDate
    private LocalDateTime qnaPostUpdatedAt;
}

