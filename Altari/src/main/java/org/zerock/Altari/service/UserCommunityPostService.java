package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserCommunityPostDTO;
import org.zerock.Altari.entity.UserCommunityPostEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.UserCommunityCommentRepository;
import org.zerock.Altari.repository.UserCommunityPostCategoryRepository;
import org.zerock.Altari.repository.UserCommunityPostRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserCommunityPostService {

    private final UserCommunityPostRepository userCommunityPostRepository;
    private final UserCommunityPostCategoryRepository userCommunityPostCategoryRepository;

    public UserCommunityPostDTO createPost(UserEntity user, UserCommunityPostDTO postDTO) {

        UserCommunityPostEntity userCommunityPostEntity = UserCommunityPostEntity.builder()
                .userCommunityPostTitle(postDTO.getUserCommunityPostTitle())
                .userCommunityPostContent(postDTO.getUserCommunityPostContent())
                .userCommunityPostLikes(postDTO.getUserCommunityPostLikes())
                .userCommunityPostViewCount(postDTO.getUserCommunityPostViewCount())
                .userCommunityPostCategory(userCommunityPostCategoryRepository.findByUserCommunityPostCategoryId(postDTO.getUserCommunityPostCategory())
                        .orElseThrow(CustomEntityExceptions.NOT_FOUND::get))
                .user(user)
                .build();

        UserCommunityPostEntity createdPost = userCommunityPostRepository.save(userCommunityPostEntity);

        return UserCommunityPostDTO.builder()
                .userCommunityPostId(createdPost.getUserCommunityPostId())
                .userCommunityPostTitle(createdPost.getUserCommunityPostTitle())
                .userCommunityPostContent(createdPost.getUserCommunityPostContent())
                .userCommunityPostLikes(createdPost.getUserCommunityPostLikes())
                .userCommunityPostViewCount(createdPost.getUserCommunityPostViewCount())
                .userCommunityPostCategory(createdPost.getUserCommunityPostCategory().getUserCommunityPostCategoryId())
                .userCommunityPostCreatedAt(createdPost.getUserCommunityPostCreatedAt())
                .build();


    }

    public UserCommunityPostDTO updatePost(Integer postId, UserCommunityPostDTO postDTO) {
        UserCommunityPostEntity postEntity = userCommunityPostRepository.findByUserCommunityPostId(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        postEntity.setUserCommunityPostTitle(postDTO.getUserCommunityPostTitle());
        postEntity.setUserCommunityPostContent(postDTO.getUserCommunityPostContent());

        UserCommunityPostEntity updatedPost = userCommunityPostRepository.save(postEntity);

        return UserCommunityPostDTO.builder()
                .userCommunityPostId(updatedPost.getUserCommunityPostId())
                .userCommunityPostTitle(updatedPost.getUserCommunityPostTitle())
                .userCommunityPostContent(updatedPost.getUserCommunityPostContent())
                .userCommunityPostLikes(updatedPost.getUserCommunityPostLikes())
                .userCommunityPostViewCount(updatedPost.getUserCommunityPostViewCount())
                .userCommunityPostCategory(updatedPost.getUserCommunityPostCategory().getUserCommunityPostCategoryId())
                .userCommunityPostCreatedAt(updatedPost.getUserCommunityPostCreatedAt())
                .build();
    }

    public UserCommunityPostDTO readPost(UserEntity user, Integer postId) {
        UserCommunityPostEntity post = userCommunityPostRepository.findByUserCommunityPostId(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        return UserCommunityPostDTO.builder()
                .userCommunityPostId(post.getUserCommunityPostId())
                .userCommunityPostTitle(post.getUserCommunityPostTitle())
                .userCommunityPostContent(post.getUserCommunityPostContent())
                .userCommunityPostLikes(post.getUserCommunityPostLikes())
                .userCommunityPostViewCount(post.getUserCommunityPostViewCount())
                .userCommunityPostCreatedAt(post.getUserCommunityPostCreatedAt())
                .userCommunityPostUpdatedAt(post.getUserCommunityPostUpdatedAt())
                .userCommunityPostCategory(post.getUserCommunityPostCategory().getUserCommunityPostCategoryId())
                .isAuthorizedUser(post.getUser().equals(user))
                .build();
    }

    public Page<UserCommunityPostDTO> readAllPosts(UserEntity user, Pageable pageable) {
        Page<UserCommunityPostEntity> posts = userCommunityPostRepository.findAll(pageable);
        // findAll 메서드에 Pageable 타입의 객체를 넣어 Page 타입으로 포스트들을 반환

        if (posts.isEmpty()) {
            throw CustomEntityExceptions.NOT_FOUND.get();
        }
        // 포스트 엔티티의 null 검사 예외처리 수행

        return posts.map(post -> UserCommunityPostDTO.builder()
                .user(post.getUser().getUsername())
                .userCommunityPostId(post.getUserCommunityPostId())
                .userCommunityPostTitle(post.getUserCommunityPostTitle())
                .userCommunityPostContent(post.getUserCommunityPostContent())
                .userCommunityPostLikes(post.getUserCommunityPostLikes())
                .userCommunityPostViewCount(post.getUserCommunityPostViewCount())
                .userCommunityPostCreatedAt(post.getUserCommunityPostCreatedAt())
                .userCommunityPostUpdatedAt(post.getUserCommunityPostUpdatedAt())
                .userCommunityPostCategory(post.getUserCommunityPostCategory().getUserCommunityPostCategoryId())
                .isAuthorizedUser(post.getUser().equals(user))
                .build());

    }

    @Transactional
    public UserCommunityPostDTO likePost(Integer postId) {
        // 게시글 조회
        UserCommunityPostEntity postEntity = userCommunityPostRepository.findByUserCommunityPostId(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 좋아요 수 증가
        postEntity.setUserCommunityPostLikes(postEntity.getUserCommunityPostLikes() + 1);

        // 변경 감지로 업데이트 수행
        userCommunityPostRepository.save(postEntity);

        // DTO 반환
        return UserCommunityPostDTO.builder()
                .userCommunityPostId(postEntity.getUserCommunityPostId())
                .userCommunityPostLikes(postEntity.getUserCommunityPostLikes()) // 업데이트된 좋아요 수 포함
                .build();
    }

    @Transactional
    public UserCommunityPostDTO unlikePost(Integer postId) {
        // 게시글 조회
        UserCommunityPostEntity postEntity = userCommunityPostRepository.findByUserCommunityPostId(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 좋아요 수 감소 (0 이하로는 감소하지 않도록 처리)
        int updatedLikes = Math.max(postEntity.getUserCommunityPostLikes() - 1, 0);
        postEntity.setUserCommunityPostLikes(updatedLikes);

        // 변경 감지로 업데이트 수행
        userCommunityPostRepository.save(postEntity);

        // DTO 반환
        return UserCommunityPostDTO.builder()
                .userCommunityPostId(postEntity.getUserCommunityPostId())
                .userCommunityPostLikes(postEntity.getUserCommunityPostLikes()) // 업데이트된 좋아요 수 포함
                .build();
    }


    public void deletePost(Integer postId) {
        UserCommunityPostEntity postEntity = userCommunityPostRepository.findByUserCommunityPostId(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        userCommunityPostRepository.delete(postEntity);
    }
}
