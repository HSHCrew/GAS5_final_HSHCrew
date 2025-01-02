package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.UserCommunityCommentDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.UserCommunityCommentService;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/altari/comments")
@Log4j2
@RequiredArgsConstructor
public class UserCommunityCommentController {

    private final UserCommunityCommentService userCommunityCommentService;
    private final JWTUtil jwtUtil;

    // 댓글 생성
    @PostMapping("/{postId}")
    public ResponseEntity<UserCommunityCommentDTO> createComment(
            @RequestBody UserCommunityCommentDTO commentDTO,
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken); // JWT 토큰에서 사용자 정보 추출
        UserCommunityCommentDTO createdComment = userCommunityCommentService.createComment(user, postId, commentDTO);

        return ResponseEntity.ok(createdComment);
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<UserCommunityCommentDTO> updateComment(
            @RequestBody UserCommunityCommentDTO commentDTO,
            @PathVariable("commentId") Integer commentId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        // 댓글 수정은 작성자인지 확인하는 로직이 추가될 수 있음 (현재는 생략)
        UserCommunityCommentDTO updatedComment = userCommunityCommentService.updateComment(commentId, commentDTO);

        return ResponseEntity.ok(updatedComment);
    }

    // 단일 댓글 조회
    @GetMapping("/{commentId}")
    public ResponseEntity<UserCommunityCommentDTO> readComment(
            @PathVariable("commentId") Integer commentId) {

        UserCommunityCommentDTO comment = userCommunityCommentService.readComment(commentId);
        return ResponseEntity.ok(comment);
    }

    // 특정 게시글의 모든 댓글 조회
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<UserCommunityCommentDTO>> readAllComments(
            @PathVariable("postId") Integer postId) {

        List<UserCommunityCommentDTO> comments = userCommunityCommentService.readAllComments(postId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable("commentId") Integer commentId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        // 댓글 삭제는 작성자인지 확인하는 로직이 추가될 수 있음 (현재는 생략)
        userCommunityCommentService.deleteComment(commentId);
        return ResponseEntity.ok("댓글 삭제가 완료되었습니다.");
    }
}
