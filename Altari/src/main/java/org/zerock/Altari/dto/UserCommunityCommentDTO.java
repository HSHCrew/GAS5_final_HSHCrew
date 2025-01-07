package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.UserCommunityPostEntity;
import org.zerock.Altari.entity.UserEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserCommunityCommentDTO {

        private Integer userCommunityCommentId;
        private Integer userCommunityPost;
        private String user;
        private Integer userCommunityParentCommentId;
        private String userCommunityCommentContent;
        private Integer userCommunityCommentLikes;
        private LocalDateTime userCommunityCommentCreatedAt;
        private LocalDateTime userCommunityCommentUpdatedAt;
        private boolean isAuthorizedUser;

}
