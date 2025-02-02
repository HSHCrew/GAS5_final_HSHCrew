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
import org.zerock.Altari.service.UserCommunityService;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/altari/userCommunity")
@Log4j2
@RequiredArgsConstructor
public class UserCommunityController {

    private final UserCommunityService userCommunityService;
    private final JWTUtil jwtUtil;

    // 커뮤니티 게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<UserCommunityPostDTO> createPost(
            @RequestBody UserCommunityPostDTO postDTO,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityPostDTO createdPost = userCommunityService.createPost(user, postDTO);

        return ResponseEntity.ok(createdPost);
    }

    // 커뮤니티 게시글 수정
    @PutMapping("/posts/{postId}")
    public ResponseEntity<UserCommunityPostDTO> updatePost(
            @RequestBody UserCommunityPostDTO postDTO,
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityPostDTO updatedPost = userCommunityService.updatePost(user, postId, postDTO);

        return ResponseEntity.ok(updatedPost);
    }

    // 단일 커뮤니티 게시글 조회
    @GetMapping("/readPosts/{postId}")
    public ResponseEntity<UserCommunityPostDTO> readPost(
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityPostDTO post = userCommunityService.readPost(user, postId);
        return ResponseEntity.ok(post);
    }

    // 모든 커뮤니티 게시글 조회
    @GetMapping("/readAllPosts")
    public ResponseEntity<Page<UserCommunityPostDTO>> readAllPosts(
            @PageableDefault(size = 20, sort = "userCommunityPostCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        Page<UserCommunityPostDTO> posts = userCommunityService.readAllPosts(user, pageable);
        return ResponseEntity.ok(posts);
    }

    // 모든 커뮤니티 게시글 조회
    @GetMapping("/usersPosts")
    public ResponseEntity<Page<UserCommunityPostDTO>> readUsersPosts(
            @PageableDefault(size = 20, sort = "userCommunityPostCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        Page<UserCommunityPostDTO> posts = userCommunityService.readUsersPosts(user, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/category/{categoryId}")
    public ResponseEntity<Page<UserCommunityPostDTO>> readPostsByCategory(
            @PathVariable("categoryId") Integer categoryId,
            @PageableDefault(size = 20, sort = "userCommunityPostCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        // 사용자 정보 추출
        UserEntity user = jwtUtil.getUserFromToken(accessToken);

        // 특정 카테고리에 속한 게시글 조회
        Page<UserCommunityPostDTO> posts = userCommunityService.readPostsByCategory(user, categoryId, pageable);

        // 결과 반환
        return ResponseEntity.ok(posts);
    }

    // 하루 기준으로 인기 게시글 조회 API
    @GetMapping("/posts/top/day")
    public ResponseEntity<Page<UserCommunityPostDTO>> readTopPostsForDay(
            @PageableDefault(size = 20, sort = "userCommunityPostCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        Page<UserCommunityPostDTO> posts = userCommunityService.readTopPostsForDay(user, pageable);
        return ResponseEntity.ok(posts);
    }

    // 일주일 기준으로 인기 게시글 조회 API
    @GetMapping("/posts/top/week")
    public ResponseEntity<Page<UserCommunityPostDTO>> readTopPostsForWeek(
            @PageableDefault(size = 20, sort = "userCommunityPostCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        Page<UserCommunityPostDTO> posts = userCommunityService.readTopPostsForWeek(user, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{keyword}")
    public ResponseEntity<Page<UserCommunityPostDTO>> searchPosts(
            @PathVariable("keyword") String keyword,
            @PageableDefault(size = 20, sort = "userCommunityPostCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        Page<UserCommunityPostDTO> posts = userCommunityService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/Category")
    public ResponseEntity<Page<UserCommunityPostDTO>> searchPostsToCategory(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @PageableDefault(size = 20, sort = "userCommunityPostCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        Page<UserCommunityPostDTO> posts = userCommunityService.searchPostsToCategory(keyword, categoryId, pageable);
        return ResponseEntity.ok(posts);
    }

    // 임시 저장된 게시글 목록 조회
    @GetMapping("/posts/draft")
    public ResponseEntity<List<UserCommunityPostDTO>> readDraftPosts(
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        List<UserCommunityPostDTO> draftPosts = userCommunityService.readDraftPosts(user);
        return ResponseEntity.ok(draftPosts);
    }

    // 게시글 작성 또는 수정 (Create or Update)
    @PostMapping("/posts/draft")
    public ResponseEntity<UserCommunityPostDTO> createOrUpdateDraftPost(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody UserCommunityPostDTO postDTO) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityPostDTO savedPost = userCommunityService.createOrUpdateDraftPost(user, postDTO);
        return ResponseEntity.ok(savedPost);
    }

    // 게시글 삭제 (Draft 포함)
    @DeleteMapping("/posts/draft/{postId}")
    public ResponseEntity<String> deleteDraftPost(
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userCommunityService.deleteDraftPost(user, postId);
        return ResponseEntity.ok("게시글 삭제가 완료되었습니다.");
    }

    // 커뮤니티 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        userCommunityService.deletePost(postId, user);
        return ResponseEntity.ok("게시글 삭제가 완료되었습니다.");
    }

    // 커뮤니티 댓글 생성
    @PostMapping("/comments/{postId}")
    public ResponseEntity<UserCommunityCommentDTO> createComment(
            @RequestBody UserCommunityCommentDTO commentDTO,
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityCommentDTO createdComment = userCommunityService.createComment(user, postId, commentDTO);

        return ResponseEntity.ok(createdComment);
    }

    // 사용자 커뮤니티 대댓글 생성
    @PostMapping("/ReplyComments/{parentCommentId}")
    public ResponseEntity<UserCommunityCommentDTO> createReplyComment(
            @RequestBody UserCommunityCommentDTO commentDTO,
            @PathVariable("parentCommentId") Integer parentCommentId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        // 사용자 정보 추출
        UserEntity user = jwtUtil.getUserFromToken(accessToken);

        // 대댓글 생성 서비스 호출
        UserCommunityCommentDTO createdReplyComment = userCommunityService.createReplyComment(user, parentCommentId, commentDTO);

        // 생성된 대댓글 DTO 반환
        return ResponseEntity.ok(createdReplyComment);
    }


    // 커뮤니티 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<UserCommunityCommentDTO> updateComment(
            @RequestBody UserCommunityCommentDTO commentDTO,
            @PathVariable("commentId") Integer commentId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserCommunityCommentDTO updatedComment = userCommunityService.updateComment(commentId, commentDTO);

        return ResponseEntity.ok(updatedComment);
    }

    // 단일 커뮤니티 댓글 조회
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<UserCommunityCommentDTO> readComment(
            @PathVariable("commentId") Integer commentId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityCommentDTO comment = userCommunityService.readComment(user, commentId);
        return ResponseEntity.ok(comment);
    }

    // 특정 게시글에 대한 모든 댓글 조회
    @GetMapping("/comments/post/{postId}")
    public ResponseEntity<List<UserCommunityCommentDTO>> readAllComments(
            @PathVariable("postId") Integer postId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        List<UserCommunityCommentDTO> comments = userCommunityService.readAllComments(user, postId);
        return ResponseEntity.ok(comments);
    }

    // 커뮤니티 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable("commentId") Integer commentId,
            @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        userCommunityService.deleteComment(commentId);
        return ResponseEntity.ok("댓글 삭제가 완료되었습니다.");
    }
}
