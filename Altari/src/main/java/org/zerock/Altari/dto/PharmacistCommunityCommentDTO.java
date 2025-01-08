package org.zerock.Altari.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PharmacistCommunityCommentDTO {

    private Integer pharmacistCommunityCommentId;     // 댓글 ID
    private Integer pharmacistCommunityPost;          // 게시글 ID (연관된 게시글)
    private String user;                        // 약사 username
    private Integer pharmacistCommunityParentCommentId; // 부모 댓글 ID
    private String pharmacistCommunityCommentContent; // 댓글 내용
    private Integer pharmacistCommunityCommentLikes;  // 좋아요 수
    private LocalDateTime pharmacistCommunityCommentCreatedAt; // 생성일
    private LocalDateTime pharmacistCommunityCommentUpdatedAt; // 수정일
    private boolean isAuthorizedUser;           // 댓글 작성 권한 확인
}
