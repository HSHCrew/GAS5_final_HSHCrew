package org.zerock.Altari.dto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.UserCommunityPostCategoryEntity;
import org.zerock.Altari.entity.UserEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserCommunityPostDTO {

    private Integer userCommunityPostId;
    private String user;
    private Boolean onComments;
    private String userCommunityPostTitle;
    private String userCommunityPostContent;
    private Integer userCommunityPostLikes;
    private Integer userCommunityPostViewCount;
    private boolean isAuthorizedUser;
    private Boolean isDraft;
    private Integer userCommunityPostCategory;
    private LocalDateTime userCommunityPostCreatedAt;
    private LocalDateTime userCommunityPostUpdatedAt;


}
