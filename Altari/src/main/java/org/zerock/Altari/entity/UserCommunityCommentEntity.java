package org.zerock.Altari.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_community_comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCommunityCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_community_comment_id")
    private Integer user_community_comment_id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_community_post_id")
    private UserCommunityPostEntity userCommunityPost;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "user_community_parent_comment_id")
    private Integer userCommunityParentCommentId;

    @Column(name = "user_community_comment_content", columnDefinition = "TEXT")
    private String userCommunityCommentContent;

    @Column(name = "user_community_comment_likes")
    private Integer userCommunityCommentLikes;


    @Column(name = "user_community_comment_created_at")
    @CreatedDate
    private LocalDateTime userCommunityCommentCreatedAt;

    @Column(name = "user_community_comment_updated_at")
    @LastModifiedDate
    private LocalDateTime userCommunityCommentUpdatedAt;


}
