package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserCommunityCommentDTO;
import org.zerock.Altari.entity.UserCommunityCommentEntity;
import org.zerock.Altari.entity.UserCommunityPostEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.UserCommunityCommentRepository;
import org.zerock.Altari.repository.UserCommunityPostRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserCommunityCommentService {

    private final UserCommunityCommentRepository userCommunityCommentRepository;
    private final UserCommunityPostRepository userCommunityPostRepository;

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

    public void deleteComment(Integer commentId) {
        UserCommunityCommentEntity commentEntity = userCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        userCommunityCommentRepository.delete(commentEntity);
    }
}

