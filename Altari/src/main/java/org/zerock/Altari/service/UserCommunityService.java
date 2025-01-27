package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserCommunityCommentDTO;
import org.zerock.Altari.dto.UserCommunityPostDTO;
import org.zerock.Altari.entity.UserCommunityCommentEntity;
import org.zerock.Altari.entity.UserCommunityPostEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.UserCommunityCommentRepository;
import org.zerock.Altari.repository.UserCommunityPostCategoryRepository;
import org.zerock.Altari.repository.UserCommunityPostRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserCommunityService {

    private final UserCommunityPostRepository userCommunityPostRepository;
    private final UserCommunityPostCategoryRepository userCommunityPostCategoryRepository;
    private final UserCommunityCommentRepository userCommunityCommentRepository;

    public UserCommunityPostDTO createPost(UserEntity user, UserCommunityPostDTO postDTO) {

        UserCommunityPostEntity userCommunityPostEntity = UserCommunityPostEntity.builder()
                .userCommunityPostTitle(postDTO.getUserCommunityPostTitle())
                .userCommunityPostContent(postDTO.getUserCommunityPostContent())
                .userCommunityPostLikes(postDTO.getUserCommunityPostLikes())
                .userCommunityPostViewCount(postDTO.getUserCommunityPostViewCount())
                .userCommunityPostCategory(userCommunityPostCategoryRepository.findByUserCommunityPostCategoryId(postDTO.getUserCommunityPostCategory())
                        .orElseThrow(CustomEntityExceptions.NOT_FOUND::get))
                .user(user)
                .onComments(postDTO.getOnComments()) // onComments 설정
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
                .onComments(createdPost.getOnComments()) // DTO에 onComments 포함
                .build();
    }

    public UserCommunityPostDTO updatePost(UserEntity user, Integer postId, UserCommunityPostDTO postDTO) {
        UserCommunityPostEntity postEntity = userCommunityPostRepository.findByUserCommunityPostId(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 유저가 본인 게시글을 수정하거나 관리자일 때만 수정 가능
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleId() == 2); // ADMIN 역할 확인

        if (!postEntity.getUser().equals(user) && !isAdmin) {
            throw CustomEntityExceptions.UNAUTHORIZED_ACCESS.get();
        }

        postEntity.setUserCommunityPostCategory(userCommunityPostCategoryRepository.findByUserCommunityPostCategoryId(postDTO.getUserCommunityPostCategory()).orElseThrow(CustomEntityExceptions.NOT_FOUND::get));
        postEntity.setUserCommunityPostTitle(postDTO.getUserCommunityPostTitle());
        postEntity.setUserCommunityPostContent(postDTO.getUserCommunityPostContent());
        postEntity.setOnComments(postDTO.getOnComments()); // onComments 업데이트

        UserCommunityPostEntity updatedPost = userCommunityPostRepository.save(postEntity);

        return UserCommunityPostDTO.builder()
                .userCommunityPostId(updatedPost.getUserCommunityPostId())
                .userCommunityPostTitle(updatedPost.getUserCommunityPostTitle())
                .userCommunityPostContent(updatedPost.getUserCommunityPostContent())
                .userCommunityPostLikes(updatedPost.getUserCommunityPostLikes())
                .userCommunityPostViewCount(updatedPost.getUserCommunityPostViewCount())
                .userCommunityPostCategory(updatedPost.getUserCommunityPostCategory().getUserCommunityPostCategoryId())
                .userCommunityPostCreatedAt(updatedPost.getUserCommunityPostCreatedAt())
                .onComments(updatedPost.getOnComments()) // DTO에 onComments 포함
                .build();
    }

    public UserCommunityPostDTO readPost(UserEntity user, Integer postId) {
        // 게시글 조회
        UserCommunityPostEntity post = userCommunityPostRepository.findByUserCommunityPostId(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 조회수 증가
        post.setUserCommunityPostViewCount(post.getUserCommunityPostViewCount() + 1);
        userCommunityPostRepository.save(post); // 변경 감지로 업데이트 수행

        // ADMIN 역할이 있는지 체크
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleId() == 2); // role_id가 2일 경우 ADMIN

        return UserCommunityPostDTO.builder()
                .userCommunityPostId(post.getUserCommunityPostId())
                .userCommunityPostTitle(post.getUserCommunityPostTitle())
                .userCommunityPostContent(post.getUserCommunityPostContent())
                .userCommunityPostLikes(post.getUserCommunityPostLikes())
                .userCommunityPostViewCount(post.getUserCommunityPostViewCount()) // 업데이트된 조회수 포함
                .userCommunityPostCreatedAt(post.getUserCommunityPostCreatedAt())
                .userCommunityPostUpdatedAt(post.getUserCommunityPostUpdatedAt())
                .userCommunityPostCategory(post.getUserCommunityPostCategory().getUserCommunityPostCategoryId())
                .isAuthorizedUser(post.getUser().equals(user) || isAdmin)
                .onComments(post.getOnComments()) // DTO에 onComments 포함
                .build();
    }

    public Page<UserCommunityPostDTO> readAllPosts(UserEntity user, Pageable pageable) {
        Page<UserCommunityPostEntity> posts = userCommunityPostRepository.findAll(pageable);

        if (posts.isEmpty()) {
            throw CustomEntityExceptions.NOT_FOUND.get();
        }

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
                .onComments(post.getOnComments()) // DTO에 onComments 포함
                .build());
    }

    public Page<UserCommunityPostDTO> readUsersPosts(UserEntity user, Pageable pageable) {

        Page<UserCommunityPostEntity> posts = userCommunityPostRepository.findByUser(user, pageable).orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        return posts.map(post -> UserCommunityPostDTO.builder()
                .user(post.getUser().getUsername())
                .userCommunityPostId(post.getUserCommunityPostId())
                .userCommunityPostTitle(post.getUserCommunityPostTitle())
                .userCommunityPostContent(post.getUserCommunityPostContent())
                .userCommunityPostViewCount(post.getUserCommunityPostViewCount())
                .userCommunityPostCreatedAt(post.getUserCommunityPostCreatedAt())
                .userCommunityPostUpdatedAt(post.getUserCommunityPostUpdatedAt())
                .userCommunityPostCategory(post.getUserCommunityPostCategory().getUserCommunityPostCategoryId())
                .isAuthorizedUser(post.getUser().equals(user))
                .onComments(post.getOnComments()) // DTO에 onComments 포함
                .build());
    }

    public Page<UserCommunityPostDTO> readPostsByCategory(UserEntity user, Integer categoryId, Pageable pageable) {
        // 주어진 카테고리 ID에 맞는 게시글을 조회
        Page<UserCommunityPostEntity> posts = userCommunityPostRepository.findByUserCommunityPostCategory(userCommunityPostCategoryRepository.findByUserCommunityPostCategoryId(categoryId).orElseThrow(CustomEntityExceptions.NOT_FOUND::get), pageable)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // DTO로 변환하여 반환
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
                .onComments(post.getOnComments()) // DTO에 onComments 포함
                .build());
    }


    public Page<UserCommunityPostDTO> readTopPostsForDay(UserEntity user, Pageable pageable) {
        // 하루 기준으로 인기 게시글을 조회 (예: 24시간 이내 작성된 게시글)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);

        // 조회수 기준 내림차순 정렬 추가
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("userCommunityPostViewCount")));

        // 정렬된 페이지로 게시글 조회
        Page<UserCommunityPostEntity> posts = userCommunityPostRepository.findByUserCommunityPostCreatedAtAfter(oneDayAgo, sortedPageable)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 인기 게시글을 DTO로 변환하여 반환
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
                .onComments(post.getOnComments()) // DTO에 onComments 포함
                .build());
    }


    public Page<UserCommunityPostDTO> readTopPostsForWeek(UserEntity user, Pageable pageable) {
        // 일주일 기준으로 인기 게시글을 조회 (예: 7일 이내 작성된 게시글)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusWeeks(1);

        // 조회수 기준 내림차순 정렬 추가
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("userCommunityPostViewCount")));

        // 정렬된 페이지로 게시글 조회
        Page<UserCommunityPostEntity> posts = userCommunityPostRepository.findByUserCommunityPostCreatedAtAfter(oneWeekAgo, sortedPageable)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 인기 게시글을 DTO로 변환하여 반환
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
                .onComments(post.getOnComments()) // DTO에 onComments 포함
                .build());
    }

    public Page<UserCommunityPostDTO> searchPosts(String keyword,
                                                  Pageable pageable) {

        Page<UserCommunityPostEntity> posts = userCommunityPostRepository.findByUserCommunityPostTitle(keyword, pageable)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

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
                .onComments(post.getOnComments()) // DTO에 onComments 포함
                .build());
    }

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

    public UserCommunityCommentDTO createComment(UserEntity user, Integer postId, UserCommunityCommentDTO commentDTO) {
        UserCommunityPostEntity postEntity = userCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        UserCommunityCommentEntity commentEntity = UserCommunityCommentEntity.builder()
                .userCommunityPost(postEntity)
                .user(user)
                .userCommunityCommentGroupId(null)
                .userCommunityCommentGroupOrder(0)
                .userCommunityCommentDepth(0)
                .userCommunityCommentContent(commentDTO.getUserCommunityCommentContent())
                .userCommunityCommentLikes(0)
                .build();

        UserCommunityCommentEntity createdComment = userCommunityCommentRepository.save(commentEntity);
        createdComment.setUserCommunityCommentGroupId(createdComment.getUserCommunityCommentId());

        return UserCommunityCommentDTO.builder()
                .userCommunityCommentId(createdComment.getUserCommunityCommentId())
                .userCommunityPost(postEntity.getUserCommunityPostId())
                .user(user.getUsername())
                .userCommunityCommentGroupId(createdComment.getUserCommunityCommentGroupId())
                .userCommunityCommentGroupOrder(createdComment.getUserCommunityCommentGroupOrder())
                .userCommunityCommentDepth(createdComment.getUserCommunityCommentDepth())
                .userCommunityCommentContent(createdComment.getUserCommunityCommentContent())
                .userCommunityCommentLikes(createdComment.getUserCommunityCommentLikes())
                .userCommunityCommentCreatedAt(createdComment.getUserCommunityCommentCreatedAt())
                .build();
    }

    public UserCommunityCommentDTO createReplyComment(UserEntity user, Integer parentCommentId, UserCommunityCommentDTO commentDTO) {
        UserCommunityCommentEntity parentCommentEntity = userCommunityCommentRepository.findById(parentCommentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        UserCommunityCommentEntity maxGroupOrderComment = userCommunityCommentRepository.findTopByUserCommunityCommentGroupIdOrderByUserCommunityCommentGroupOrderDesc(parentCommentEntity.getUserCommunityCommentGroupId())
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        UserCommunityCommentEntity commentEntity = UserCommunityCommentEntity.builder()
                .userCommunityPost(parentCommentEntity.getUserCommunityPost())
                .user(user)
                .userCommunityCommentGroupId(parentCommentEntity.getUserCommunityCommentGroupId())
                .userCommunityCommentGroupOrder(maxGroupOrderComment.getUserCommunityCommentGroupOrder())
                .userCommunityCommentDepth(1)
                .userCommunityCommentContent(commentDTO.getUserCommunityCommentContent())
                .userCommunityCommentLikes(0)
                .build();

        UserCommunityCommentEntity createdReplyComment = userCommunityCommentRepository.save(commentEntity);

        return UserCommunityCommentDTO.builder()
                .userCommunityCommentId(createdReplyComment.getUserCommunityCommentId())
                .userCommunityPost(createdReplyComment.getUserCommunityPost().getUserCommunityPostId())
                .user(user.getUsername())
                .userCommunityCommentGroupId(createdReplyComment.getUserCommunityCommentGroupId())
                .userCommunityCommentGroupOrder(createdReplyComment.getUserCommunityCommentGroupOrder())
                .userCommunityCommentDepth(createdReplyComment.getUserCommunityCommentDepth())
                .userCommunityCommentContent(createdReplyComment.getUserCommunityCommentContent())
                .userCommunityCommentLikes(createdReplyComment.getUserCommunityCommentLikes())
                .userCommunityCommentCreatedAt(createdReplyComment.getUserCommunityCommentCreatedAt())
                .build();
    }

    public UserCommunityCommentDTO updateComment(Integer commentId, UserCommunityCommentDTO commentDTO) {
        UserCommunityCommentEntity commentEntity = userCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        commentEntity.setUserCommunityCommentContent(commentDTO.getUserCommunityCommentContent());

        UserCommunityCommentEntity updatedComment = userCommunityCommentRepository.save(commentEntity);

        return UserCommunityCommentDTO.builder()
                .userCommunityCommentId(updatedComment.getUserCommunityCommentId())
                .userCommunityPost(updatedComment.getUserCommunityPost().getUserCommunityPostId())
                .user(updatedComment.getUser().getUsername())
                .userCommunityCommentGroupId(updatedComment.getUserCommunityCommentGroupId())
                .userCommunityCommentGroupOrder(updatedComment.getUserCommunityCommentGroupOrder())
                .userCommunityCommentDepth(updatedComment.getUserCommunityCommentDepth())
                .userCommunityCommentContent(updatedComment.getUserCommunityCommentContent())
                .userCommunityCommentLikes(updatedComment.getUserCommunityCommentLikes())
                .userCommunityCommentCreatedAt(updatedComment.getUserCommunityCommentCreatedAt())
                .userCommunityCommentUpdatedAt(updatedComment.getUserCommunityCommentUpdatedAt())
                .build();
    }

    public UserCommunityCommentDTO readComment(UserEntity user, Integer commentId) {

        UserCommunityCommentEntity commentEntity = userCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);


        return UserCommunityCommentDTO.builder()
                .userCommunityCommentId(commentEntity.getUserCommunityCommentId())
                .userCommunityPost(commentEntity.getUserCommunityPost().getUserCommunityPostId())
                .user(commentEntity.getUser().getUsername())
                .userCommunityCommentGroupId(commentEntity.getUserCommunityCommentGroupId())
                .userCommunityCommentGroupOrder(commentEntity.getUserCommunityCommentGroupOrder())
                .userCommunityCommentDepth(commentEntity.getUserCommunityCommentDepth())
                .userCommunityCommentContent(commentEntity.getUserCommunityCommentContent())
                .userCommunityCommentLikes(commentEntity.getUserCommunityCommentLikes())
                .userCommunityCommentCreatedAt(commentEntity.getUserCommunityCommentCreatedAt())
                .userCommunityCommentUpdatedAt(commentEntity.getUserCommunityCommentUpdatedAt())
                .isAuthorizedUser(commentEntity.getUser().equals(user))
                .build();
    }

    public List<UserCommunityCommentDTO> readAllComments(UserEntity user, Integer postId) {
        UserCommunityPostEntity postEntity = userCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        List<UserCommunityCommentEntity> comments = userCommunityCommentRepository.findByUserCommunityPost(postEntity)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);


        return comments.stream().map(comment -> UserCommunityCommentDTO.builder()
                        .userCommunityCommentId(comment.getUserCommunityCommentId())
                        .userCommunityPost(comment.getUserCommunityPost().getUserCommunityPostId())
                        .user(comment.getUser().getUsername())
                        .userCommunityCommentGroupId(comment.getUserCommunityCommentGroupId())
                        .userCommunityCommentGroupOrder(comment.getUserCommunityCommentGroupOrder())
                        .userCommunityCommentDepth(comment.getUserCommunityCommentDepth())
                        .userCommunityCommentContent(comment.getUserCommunityCommentContent())
                        .userCommunityCommentLikes(comment.getUserCommunityCommentLikes())
                        .userCommunityCommentCreatedAt(comment.getUserCommunityCommentCreatedAt())
                        .userCommunityCommentUpdatedAt(comment.getUserCommunityCommentUpdatedAt())
                        .isAuthorizedUser(comment.getUser().equals(user))
                        .build())
                .collect(Collectors.toList());
    }

    // 댓글 좋아요 추가
    public UserCommunityCommentDTO likeComment(Integer commentId) {
        UserCommunityCommentEntity commentEntity = userCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 좋아요 수 증가
        commentEntity.setUserCommunityCommentLikes(commentEntity.getUserCommunityCommentLikes() + 1);

        // 저장 및 DTO 반환
        userCommunityCommentRepository.save(commentEntity);
        return UserCommunityCommentDTO.builder()
                .userCommunityCommentId(commentEntity.getUserCommunityCommentId())
                .userCommunityCommentLikes(commentEntity.getUserCommunityCommentLikes())
                .build();
    }

    // 댓글 좋아요 취소
    public UserCommunityCommentDTO unlikeComment(Integer commentId) {
        UserCommunityCommentEntity commentEntity = userCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 좋아요 수 감소 (0 이하로는 감소하지 않도록 처리)
        int updatedLikes = Math.max(commentEntity.getUserCommunityCommentLikes() - 1, 0);
        commentEntity.setUserCommunityCommentLikes(updatedLikes);

        // 저장 및 DTO 반환
        userCommunityCommentRepository.save(commentEntity);

        return UserCommunityCommentDTO.builder()
                .userCommunityCommentId(commentEntity.getUserCommunityCommentId())
                .userCommunityCommentLikes(commentEntity.getUserCommunityCommentLikes())
                .build();
    }

    public void deleteComment(Integer commentId) {
        UserCommunityCommentEntity commentEntity = userCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        userCommunityCommentRepository.delete(commentEntity);
    }
}
