package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.UserCommunityPostDTO;
import org.zerock.Altari.dto.UserCommunityCommentDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.UserCommunityPostService;
import org.zerock.Altari.service.UserCommunityCommentService;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/altari/userCommunity")
@Log4j2
@RequiredArgsConstructor
public class UserCommunityController {

    private final UserCommunityPostService userCommunityPostService;
    private final UserCommunityCommentService userCommunityCommentService;
    private final JWTUtil jwtUtil;

    // 커뮤니티 게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<UserCommunityPostDTO> createPost(
            @RequestBody UserCommunityPostDTO postDTO,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityPostDTO createdPost = userCommunityPostService.createPost(user, postDTO);

        return ResponseEntity.ok(createdPost);
    }

    // 커뮤니티 게시글 수정
    @PutMapping("/posts/{postId}")
    public ResponseEntity<UserCommunityPostDTO> updatePost(
            @RequestBody UserCommunityPostDTO postDTO,
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserCommunityPostDTO updatedPost = userCommunityPostService.updatePost(postId, postDTO);

        return ResponseEntity.ok(updatedPost);
    }

    // 단일 커뮤니티 게시글 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<UserCommunityPostDTO> readPost(
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityPostDTO post = userCommunityPostService.readPost(user, postId);
        return ResponseEntity.ok(post);
    }

    // 모든 커뮤니티 게시글 조회
    @GetMapping("/posts")
    public ResponseEntity<Page<UserCommunityPostDTO>> readAllPosts(
            @PageableDefault(size = 20, sort = "userCommunityPostCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        Page<UserCommunityPostDTO> posts = userCommunityPostService.readAllPosts(user, pageable);
        return ResponseEntity.ok(posts);
    }

    // 커뮤니티 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        userCommunityPostService.deletePost(postId);
        return ResponseEntity.ok("게시글 삭제가 완료되었습니다.");
    }

    // 커뮤니티 댓글 생성
    @PostMapping("/comments/{postId}")
    public ResponseEntity<UserCommunityCommentDTO> createComment(
            @RequestBody UserCommunityCommentDTO commentDTO,
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityCommentDTO createdComment = userCommunityCommentService.createComment(user, postId, commentDTO);

        return ResponseEntity.ok(createdComment);
    }

    // 커뮤니티 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<UserCommunityCommentDTO> updateComment(
            @RequestBody UserCommunityCommentDTO commentDTO,
            @PathVariable("commentId") Integer commentId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserCommunityCommentDTO updatedComment = userCommunityCommentService.updateComment(commentId, commentDTO);

        return ResponseEntity.ok(updatedComment);
    }

    // 단일 커뮤니티 댓글 조회
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<UserCommunityCommentDTO> readComment(
            @PathVariable("commentId") Integer commentId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityCommentDTO comment = userCommunityCommentService.readComment(user, commentId);
        return ResponseEntity.ok(comment);
    }

    // 특정 게시글에 대한 모든 댓글 조회
    @GetMapping("/comments/post/{postId}")
    public ResponseEntity<List<UserCommunityCommentDTO>> readAllComments(
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        List<UserCommunityCommentDTO> comments = userCommunityCommentService.readAllComments(user, postId);
        return ResponseEntity.ok(comments);
    }

    // 커뮤니티 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable("commentId") Integer commentId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        userCommunityCommentService.deleteComment(commentId);
        return ResponseEntity.ok("댓글 삭제가 완료되었습니다.");
    }
}
