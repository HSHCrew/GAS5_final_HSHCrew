package org.zerock.Altari.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PharmacistCommunityPostDTO {

    private Integer pharmacistCommunityPostId; // 게시글 ID
    private String user;                // 약사 username
    private String pharmacistCommunityPostTitle; // 게시글 제목
    private String pharmacistCommunityPostContent; // 게시글 내용
    private Integer pharmacistCommunityPostLikes;  // 좋아요 수
    private Integer pharmacistCommunityPostViewCount; // 조회 수
    private boolean isAuthorizedUser;      // 게시글 작성 권한 확인
    private Integer pharmacistCommunityPostCategory; // 카테고리 ID
    private LocalDateTime pharmacistCommunityPostCreatedAt; // 생성일
    private LocalDateTime pharmacistCommunityPostUpdatedAt; // 수정일
}
