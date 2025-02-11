package org.zerock.Altari.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_community_post")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserCommunityPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_community_post_id")
    private Integer userCommunityPostId;

    @Version
    private Integer version;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "on_comments")
    private Boolean onComments;

    @Column(name = "is_draft")
    private Boolean isDraft;

    @Column(name = "user_community_post_title", columnDefinition = "TEXT")
    private String userCommunityPostTitle;

    @Column(name = "user_community_post_content", columnDefinition = "TEXT")
    private String userCommunityPostContent;

    @ManyToOne
    @JoinColumn(name = "user_community_post_category_id")
    private UserCommunityPostCategoryEntity userCommunityPostCategory;

    @Column(name = "user_community_post_likes")
    private Integer userCommunityPostLikes;

    @Column(name = "user_community_post_view_count")
    private Integer userCommunityPostViewCount;

    @Column(name = "user_community_post_created_at")
    @CreatedDate
    private LocalDateTime userCommunityPostCreatedAt;

    @Column(name = "user_community_post_updated_at")
    @LastModifiedDate
    private LocalDateTime userCommunityPostUpdatedAt;

}
