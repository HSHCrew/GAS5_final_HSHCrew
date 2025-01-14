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
@Table(name = "pharmacist_community_comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacistCommunityCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pharmacist_community_comment_id")
    private Integer pharmacistCommunityCommentId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "pharmacist_community_post_id")
    private PharmacistCommunityPostEntity pharmacistCommunityPost;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "pharmacist_community_comment_group_id")
    private Integer pharmacistCommunityCommentGroupId;

    @Column(name = "pharmacist_community_comment_group_order")
    private Integer pharmacistCommunityCommentGroupOrder;

    @Column(name = "pharmacist_community_comment_depth")
    private Integer pharmacistCommunityCommentDepth;

    @Column(name = "pharmacist_community_comment_content", columnDefinition = "TEXT")
    private String pharmacistCommunityCommentContent;

    @Column(name = "pharmacist_community_comment_likes")
    private Integer pharmacistCommunityCommentLikes;

    @Column(name = "pharmacist_community_comment_created_at")
    @CreatedDate
    private LocalDateTime pharmacistCommunityCommentCreatedAt;

    @Column(name = "pharmacist_community_comment_updated_at")
    @LastModifiedDate
    private LocalDateTime pharmacistCommunityCommentUpdatedAt;
}

