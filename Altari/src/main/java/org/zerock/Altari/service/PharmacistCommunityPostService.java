package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.PharmacistCommunityPostDTO;
import org.zerock.Altari.entity.PharmacistCommunityPostEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.PharmacistCommunityPostCategoryRepository;
import org.zerock.Altari.repository.PharmacistCommunityPostRepository;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class PharmacistCommunityPostService {

    @Autowired
    private final PharmacistCommunityPostRepository pharmacistCommunityPostRepository;
    private final PharmacistCommunityPostCategoryRepository pharmacistCommunityPostCategoryRepository;

    // 포스트 생성 메서드
    public PharmacistCommunityPostDTO createPost(UserEntity user, PharmacistCommunityPostDTO postDTO) {
        PharmacistCommunityPostEntity pharmacistCommunityPostEntity = PharmacistCommunityPostEntity.builder()
                .pharmacistCommunityPostTitle(postDTO.getPharmacistCommunityPostTitle())
                .pharmacistCommunityPostContent(postDTO.getPharmacistCommunityPostContent())
                .pharmacistCommunityPostLikes(postDTO.getPharmacistCommunityPostLikes())
                .pharmacistCommunityPostViewCount(postDTO.getPharmacistCommunityPostViewCount())
                .onComments(postDTO.getOnComments()) // 추가된 필드 설정
                .pharmacistCommunityPostCategory(pharmacistCommunityPostCategoryRepository.findById(postDTO.getPharmacistCommunityPostCategory())
                        .orElseThrow(CustomEntityExceptions.NOT_FOUND::get))
                .user(user)
                .build();

        PharmacistCommunityPostEntity createdPost = pharmacistCommunityPostRepository.save(pharmacistCommunityPostEntity);

        return PharmacistCommunityPostDTO.builder()
                .pharmacistCommunityPostId(createdPost.getPharmacistCommunityPostId())
                .pharmacistCommunityPostTitle(createdPost.getPharmacistCommunityPostTitle())
                .pharmacistCommunityPostContent(createdPost.getPharmacistCommunityPostContent())
                .pharmacistCommunityPostLikes(createdPost.getPharmacistCommunityPostLikes())
                .pharmacistCommunityPostViewCount(createdPost.getPharmacistCommunityPostViewCount())
                .onComments(createdPost.getOnComments()) // 반환 DTO에 포함
                .pharmacistCommunityPostCategory(createdPost.getPharmacistCommunityPostCategory().getPharmacistCommunityPostCategoryId())
                .pharmacistCommunityPostCreatedAt(createdPost.getPharmacistCommunityPostCreatedAt())
                .build();
    }

    // 포스트 업데이트 메서드
    public PharmacistCommunityPostDTO updatePost(Integer postId, PharmacistCommunityPostDTO postDTO) {
        PharmacistCommunityPostEntity postEntity = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        postEntity.setPharmacistCommunityPostTitle(postDTO.getPharmacistCommunityPostTitle());
        postEntity.setPharmacistCommunityPostContent(postDTO.getPharmacistCommunityPostContent());
        postEntity.setOnComments(postDTO.getOnComments()); // 추가된 필드 업데이트

        PharmacistCommunityPostEntity updatedPost = pharmacistCommunityPostRepository.save(postEntity);

        return PharmacistCommunityPostDTO.builder()
                .pharmacistCommunityPostId(updatedPost.getPharmacistCommunityPostId())
                .pharmacistCommunityPostTitle(updatedPost.getPharmacistCommunityPostTitle())
                .pharmacistCommunityPostContent(updatedPost.getPharmacistCommunityPostContent())
                .pharmacistCommunityPostLikes(updatedPost.getPharmacistCommunityPostLikes())
                .pharmacistCommunityPostViewCount(updatedPost.getPharmacistCommunityPostViewCount())
                .onComments(updatedPost.getOnComments()) // 반환 DTO에 포함
                .pharmacistCommunityPostCategory(updatedPost.getPharmacistCommunityPostCategory().getPharmacistCommunityPostCategoryId())
                .pharmacistCommunityPostCreatedAt(updatedPost.getPharmacistCommunityPostCreatedAt())
                .build();
    }

    // 포스트 읽기 메서드
    public PharmacistCommunityPostDTO readPost(UserEntity user, Integer postId) {
        PharmacistCommunityPostEntity post = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        return PharmacistCommunityPostDTO.builder()
                .pharmacistCommunityPostId(post.getPharmacistCommunityPostId())
                .pharmacistCommunityPostTitle(post.getPharmacistCommunityPostTitle())
                .pharmacistCommunityPostContent(post.getPharmacistCommunityPostContent())
                .pharmacistCommunityPostLikes(post.getPharmacistCommunityPostLikes())
                .pharmacistCommunityPostViewCount(post.getPharmacistCommunityPostViewCount())
                .onComments(post.getOnComments()) // 반환 DTO에 포함
                .pharmacistCommunityPostCreatedAt(post.getPharmacistCommunityPostCreatedAt())
                .pharmacistCommunityPostUpdatedAt(post.getPharmacistCommunityPostUpdatedAt())
                .pharmacistCommunityPostCategory(post.getPharmacistCommunityPostCategory().getPharmacistCommunityPostCategoryId())
                .isAuthorizedUser(post.getUser().equals(user))
                .build();
    }

    // 모든 포스트 읽기 메서드
    public Page<PharmacistCommunityPostDTO> readAllPosts(UserEntity user, Pageable pageable) {
        Page<PharmacistCommunityPostEntity> posts = pharmacistCommunityPostRepository.findAll(pageable);

        if (posts.isEmpty()) {
            throw CustomEntityExceptions.NOT_FOUND.get();
        }

        return posts.map(post -> PharmacistCommunityPostDTO.builder()
                .user(post.getUser().getUsername())
                .pharmacistCommunityPostId(post.getPharmacistCommunityPostId())
                .pharmacistCommunityPostTitle(post.getPharmacistCommunityPostTitle())
                .pharmacistCommunityPostContent(post.getPharmacistCommunityPostContent())
                .pharmacistCommunityPostLikes(post.getPharmacistCommunityPostLikes())
                .pharmacistCommunityPostViewCount(post.getPharmacistCommunityPostViewCount())
                .onComments(post.getOnComments()) // 반환 DTO에 포함
                .pharmacistCommunityPostCreatedAt(post.getPharmacistCommunityPostCreatedAt())
                .pharmacistCommunityPostUpdatedAt(post.getPharmacistCommunityPostUpdatedAt())
                .pharmacistCommunityPostCategory(post.getPharmacistCommunityPostCategory().getPharmacistCommunityPostCategoryId())
                .isAuthorizedUser(post.getUser().equals(user))
                .build());
    }

    // 포스트 좋아요 증가 메서드
    public PharmacistCommunityPostDTO likePost(Integer postId) {
        // 포스트를 가져옵니다.
        PharmacistCommunityPostEntity postEntity = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 좋아요 수 증가
        postEntity.setPharmacistCommunityPostLikes(postEntity.getPharmacistCommunityPostLikes() + 1);

        // 저장된 포스트 엔티티를 반환
        PharmacistCommunityPostEntity updatedPost = pharmacistCommunityPostRepository.save(postEntity);

        // DTO로 반환
        return PharmacistCommunityPostDTO.builder()
                .pharmacistCommunityPostId(updatedPost.getPharmacistCommunityPostId())
                .pharmacistCommunityPostLikes(updatedPost.getPharmacistCommunityPostLikes())
                .build();
    }

    // 포스트 좋아요 취소 메서드
    public PharmacistCommunityPostDTO unlikePost(Integer postId) {
        // 포스트를 가져옵니다.
        PharmacistCommunityPostEntity postEntity = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 좋아요 수 감소 (0 이하로는 감소하지 않도록 처리)
        int updatedLikes = Math.max(postEntity.getPharmacistCommunityPostLikes() - 1, 0);
        postEntity.setPharmacistCommunityPostLikes(updatedLikes);

        // 저장된 포스트 엔티티를 반환
        PharmacistCommunityPostEntity updatedPost = pharmacistCommunityPostRepository.save(postEntity);

        // DTO로 반환
        return PharmacistCommunityPostDTO.builder()
                .pharmacistCommunityPostId(updatedPost.getPharmacistCommunityPostId())
                .pharmacistCommunityPostLikes(updatedPost.getPharmacistCommunityPostLikes())
                .build();
    }

    public void deletePost(Integer postId) {
        PharmacistCommunityPostEntity postEntity = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        pharmacistCommunityPostRepository.delete(postEntity);
    }
}
