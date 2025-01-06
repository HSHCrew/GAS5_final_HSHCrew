package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.UserCommunityPostDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.UserCommunityPostService;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class UserCommunityPostController {

    @Autowired
    private final UserCommunityPostService userCommunityPostService;
    private final JWTUtil jwtUtil;

    @PostMapping("/userCommunityPosts")
    public ResponseEntity<UserCommunityPostDTO> createPost(@RequestBody UserCommunityPostDTO userCommunityPostDTO,
                                                           @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        UserCommunityPostDTO createdPost = userCommunityPostService.createPost(user, userCommunityPostDTO);

        return ResponseEntity.ok(createdPost);
    }

    @PutMapping("/userCommunityPosts/{postId}")
    public ResponseEntity<UserCommunityPostDTO> updatePost(@RequestBody UserCommunityPostDTO userCommunityPostDTO,
                                                           @PathVariable("postId") Integer postId,
                                                           @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserCommunityPostDTO updatedPost = userCommunityPostService.updatePost(postId, userCommunityPostDTO);

        return ResponseEntity.ok(updatedPost);
    }

    @GetMapping("/userCommunityPosts/{postId}")
    public ResponseEntity<UserCommunityPostDTO> readPost(
            @PathVariable("postId") Integer postId
    ) throws UnsupportedEncodingException {

        UserCommunityPostDTO userCommunityPost = userCommunityPostService.readPost(postId);
        return ResponseEntity.ok(userCommunityPost);
    }

    @GetMapping("/userCommunityPosts")
    public ResponseEntity<Page<UserCommunityPostDTO>> readAllPosts(
            @PageableDefault(size = 20, sort = "userCommunityPostCreatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<UserCommunityPostDTO> userCommunityPosts = userCommunityPostService.readAllPosts(pageable);
        return ResponseEntity.ok(userCommunityPosts);
    }

    @DeleteMapping("/userCommunityPosts/{postId}")
    public ResponseEntity<String> deletePost(@RequestBody UserCommunityPostDTO userCommunityPostDTO,
                                             @PathVariable("postId") Integer postId,
                                             @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        userCommunityPostService.deletePost(postId);
        return ResponseEntity.ok("게시글 삭제가 완료되었습니다.");
    }


}
