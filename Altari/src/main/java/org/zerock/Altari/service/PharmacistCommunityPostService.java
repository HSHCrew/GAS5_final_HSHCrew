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

    public PharmacistCommunityPostDTO createPost(UserEntity user, PharmacistCommunityPostDTO postDTO) {

        PharmacistCommunityPostEntity pharmacistCommunityPostEntity = PharmacistCommunityPostEntity.builder()
                .pharmacistCommunityPostTitle(postDTO.getPharmacistCommunityPostTitle())
                .pharmacistCommunityPostContent(postDTO.getPharmacistCommunityPostContent())
                .pharmacistCommunityPostLikes(postDTO.getPharmacistCommunityPostLikes())
                .pharmacistCommunityPostViewCount(postDTO.getPharmacistCommunityPostViewCount())
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
                .pharmacistCommunityPostCategory(createdPost.getPharmacistCommunityPostCategory().getPharmacistCommunityPostCategoryId())
                .pharmacistCommunityPostCreatedAt(createdPost.getPharmacistCommunityPostCreatedAt())
                .build();
    }

    public PharmacistCommunityPostDTO updatePost(Integer postId, PharmacistCommunityPostDTO postDTO) {
        PharmacistCommunityPostEntity postEntity = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        postEntity.setPharmacistCommunityPostTitle(postDTO.getPharmacistCommunityPostTitle());
        postEntity.setPharmacistCommunityPostContent(postDTO.getPharmacistCommunityPostContent());

        PharmacistCommunityPostEntity updatedPost = pharmacistCommunityPostRepository.save(postEntity);

        return PharmacistCommunityPostDTO.builder()
                .pharmacistCommunityPostId(updatedPost.getPharmacistCommunityPostId())
                .pharmacistCommunityPostTitle(updatedPost.getPharmacistCommunityPostTitle())
                .pharmacistCommunityPostContent(updatedPost.getPharmacistCommunityPostContent())
                .pharmacistCommunityPostLikes(updatedPost.getPharmacistCommunityPostLikes())
                .pharmacistCommunityPostViewCount(updatedPost.getPharmacistCommunityPostViewCount())
                .pharmacistCommunityPostCategory(updatedPost.getPharmacistCommunityPostCategory().getPharmacistCommunityPostCategoryId())
                .pharmacistCommunityPostCreatedAt(updatedPost.getPharmacistCommunityPostCreatedAt())
                .build();
    }

    public PharmacistCommunityPostDTO readPost(UserEntity user, Integer postId) {
        PharmacistCommunityPostEntity post = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        return PharmacistCommunityPostDTO.builder()
                .pharmacistCommunityPostId(post.getPharmacistCommunityPostId())
                .pharmacistCommunityPostTitle(post.getPharmacistCommunityPostTitle())
                .pharmacistCommunityPostContent(post.getPharmacistCommunityPostContent())
                .pharmacistCommunityPostLikes(post.getPharmacistCommunityPostLikes())
                .pharmacistCommunityPostViewCount(post.getPharmacistCommunityPostViewCount())
                .pharmacistCommunityPostCreatedAt(post.getPharmacistCommunityPostCreatedAt())
                .pharmacistCommunityPostUpdatedAt(post.getPharmacistCommunityPostUpdatedAt())
                .pharmacistCommunityPostCategory(post.getPharmacistCommunityPostCategory().getPharmacistCommunityPostCategoryId())
                .isAuthorizedUser(post.getUser().equals(user))
                .build();
    }

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
                .pharmacistCommunityPostCreatedAt(post.getPharmacistCommunityPostCreatedAt())
                .pharmacistCommunityPostUpdatedAt(post.getPharmacistCommunityPostUpdatedAt())
                .pharmacistCommunityPostCategory(post.getPharmacistCommunityPostCategory().getPharmacistCommunityPostCategoryId())
                .isAuthorizedUser(post.getUser().equals(user))
                .build());
    }

    public void deletePost(Integer postId) {
        PharmacistCommunityPostEntity postEntity = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        pharmacistCommunityPostRepository.delete(postEntity);
    }
}
