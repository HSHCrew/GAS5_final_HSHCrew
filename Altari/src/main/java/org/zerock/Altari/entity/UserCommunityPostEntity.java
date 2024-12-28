package org.zerock.Altari.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_community_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCommunityPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_community_post_id")
    private Integer userCommunityPostId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "user_community_post_title", columnDefinition = "TEXT")
    private String userCommunityPostTitle;

    @Column(name = "user_community_post_content", columnDefinition = "TEXT")
    private String userCommunityPostContent;

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
