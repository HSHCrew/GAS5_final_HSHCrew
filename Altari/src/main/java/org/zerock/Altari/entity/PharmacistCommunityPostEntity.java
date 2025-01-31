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
@Table(name = "pharmacist_community_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PharmacistCommunityPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pharmacist_community_post_id")
    private Integer pharmacistCommunityPostId;

    @Version
    private Integer version;

    @Column(name = "on_comments")
    private Boolean onComments;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private UserEntity user; // 작성자 (약사)

    @Column(name = "pharmacist_community_post_title", columnDefinition = "TEXT")
    private String pharmacistCommunityPostTitle;

    @Column(name = "pharmacist_community_post_content", columnDefinition = "TEXT")
    private String pharmacistCommunityPostContent;

    @ManyToOne
    @JoinColumn(name = "pharmacist_community_post_category_id")
    private PharmacistCommunityPostCategoryEntity pharmacistCommunityPostCategory;

    @Column(name = "pharmacist_community_post_likes")
    private Integer pharmacistCommunityPostLikes;

    @Column(name = "pharmacist_community_post_view_count")
    private Integer pharmacistCommunityPostViewCount;

    @Column(name = "pharmacist_community_post_created_at")
    @CreatedDate
    private LocalDateTime pharmacistCommunityPostCreatedAt;

    @Column(name = "pharmacist_community_post_updated_at")
    @LastModifiedDate
    private LocalDateTime pharmacistCommunityPostUpdatedAt;
}
