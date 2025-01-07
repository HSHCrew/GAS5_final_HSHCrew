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
                .userCommunityParentCommentId(commentDTO.getUserCommunityParentCommentId())
                .userCommunityCommentContent(commentDTO.getUserCommunityCommentContent())
                .userCommunityCommentLikes(0)
                .build();

        UserCommunityCommentEntity createdComment = userCommunityCommentRepository.save(commentEntity);

        return UserCommunityCommentDTO.builder()
                .userCommunityCommentId(createdComment.getUserCommunityCommentId())
                .userCommunityPost(postEntity.getUserCommunityPostId())
                .user(user.getUsername())
                .userCommunityParentCommentId(createdComment.getUserCommunityParentCommentId())
                .userCommunityCommentContent(createdComment.getUserCommunityCommentContent())
                .userCommunityCommentLikes(createdComment.getUserCommunityCommentLikes())
                .userCommunityCommentCreatedAt(createdComment.getUserCommunityCommentCreatedAt())
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
                .userCommunityParentCommentId(updatedComment.getUserCommunityParentCommentId())
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
                .userCommunityParentCommentId(commentEntity.getUserCommunityParentCommentId())
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
                        .userCommunityParentCommentId(comment.getUserCommunityParentCommentId())
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

