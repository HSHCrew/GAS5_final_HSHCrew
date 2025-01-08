package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.PharmacistCommunityCommentDTO;
import org.zerock.Altari.entity.UserCommunityPostEntity;
import org.zerock.Altari.entity.UserCommunityCommentEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.UserCommunityCommentRepository;
import org.zerock.Altari.repository.UserCommunityPostRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class PharmacistCommunityCommentService {

    private final UserCommunityCommentRepository userCommunityCommentRepository;
    private final UserCommunityPostRepository userCommunityPostRepository;

    public PharmacistCommunityCommentDTO createComment(UserEntity pharmacist, Integer postId, PharmacistCommunityCommentDTO commentDTO) {
        UserCommunityPostEntity postEntity = userCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        UserCommunityCommentEntity commentEntity = UserCommunityCommentEntity.builder()
                .userCommunityPost(postEntity)
                .user(pharmacist)
                .userCommunityParentCommentId(commentDTO.getPharmacistCommunityParentCommentId())
                .userCommunityCommentContent(commentDTO.getPharmacistCommunityCommentContent())
                .userCommunityCommentLikes(0)
                .build();

        UserCommunityCommentEntity createdComment = userCommunityCommentRepository.save(commentEntity);

        return PharmacistCommunityCommentDTO.builder()
                .pharmacistCommunityCommentId(createdComment.getUserCommunityCommentId())
                .pharmacistCommunityPost(postEntity.getUserCommunityPostId())
                .user(pharmacist.getUsername())
                .pharmacistCommunityParentCommentId(createdComment.getUserCommunityParentCommentId())
                .pharmacistCommunityCommentContent(createdComment.getUserCommunityCommentContent())
                .pharmacistCommunityCommentLikes(createdComment.getUserCommunityCommentLikes())
                .pharmacistCommunityCommentCreatedAt(createdComment.getUserCommunityCommentCreatedAt())
                .build();
    }

    public PharmacistCommunityCommentDTO updateComment(Integer commentId, PharmacistCommunityCommentDTO commentDTO) {
        UserCommunityCommentEntity commentEntity = userCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        commentEntity.setUserCommunityCommentContent(commentDTO.getPharmacistCommunityCommentContent());

        UserCommunityCommentEntity updatedComment = userCommunityCommentRepository.save(commentEntity);

        return PharmacistCommunityCommentDTO.builder()
                .pharmacistCommunityCommentId(updatedComment.getUserCommunityCommentId())
                .pharmacistCommunityPost(updatedComment.getUserCommunityPost().getUserCommunityPostId())
                .user(updatedComment.getUser().getUsername())
                .pharmacistCommunityParentCommentId(updatedComment.getUserCommunityParentCommentId())
                .pharmacistCommunityCommentContent(updatedComment.getUserCommunityCommentContent())
                .pharmacistCommunityCommentLikes(updatedComment.getUserCommunityCommentLikes())
                .pharmacistCommunityCommentCreatedAt(updatedComment.getUserCommunityCommentCreatedAt())
                .pharmacistCommunityCommentUpdatedAt(updatedComment.getUserCommunityCommentUpdatedAt())
                .build();
    }

    public PharmacistCommunityCommentDTO readComment(UserEntity pharmacist, Integer commentId) {
        UserCommunityCommentEntity commentEntity = userCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        return PharmacistCommunityCommentDTO.builder()
                .pharmacistCommunityCommentId(commentEntity.getUserCommunityCommentId())
                .pharmacistCommunityPost(commentEntity.getUserCommunityPost().getUserCommunityPostId())
                .user(commentEntity.getUser().getUsername())
                .pharmacistCommunityParentCommentId(commentEntity.getUserCommunityParentCommentId())
                .pharmacistCommunityCommentContent(commentEntity.getUserCommunityCommentContent())
                .pharmacistCommunityCommentLikes(commentEntity.getUserCommunityCommentLikes())
                .pharmacistCommunityCommentCreatedAt(commentEntity.getUserCommunityCommentCreatedAt())
                .pharmacistCommunityCommentUpdatedAt(commentEntity.getUserCommunityCommentUpdatedAt())
                .isAuthorizedUser(commentEntity.getUser().equals(pharmacist))
                .build();
    }

    public List<PharmacistCommunityCommentDTO> readAllComments(UserEntity pharmacist, Integer postId) {
        UserCommunityPostEntity postEntity = userCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        List<UserCommunityCommentEntity> comments = userCommunityCommentRepository.findByUserCommunityPost(postEntity)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        return comments.stream().map(comment -> PharmacistCommunityCommentDTO.builder()
                        .pharmacistCommunityCommentId(comment.getUserCommunityCommentId())
                        .pharmacistCommunityPost(comment.getUserCommunityPost().getUserCommunityPostId())
                        .user(comment.getUser().getUsername())
                        .pharmacistCommunityParentCommentId(comment.getUserCommunityParentCommentId())
                        .pharmacistCommunityCommentContent(comment.getUserCommunityCommentContent())
                        .pharmacistCommunityCommentLikes(comment.getUserCommunityCommentLikes())
                        .pharmacistCommunityCommentCreatedAt(comment.getUserCommunityCommentCreatedAt())
                        .pharmacistCommunityCommentUpdatedAt(comment.getUserCommunityCommentUpdatedAt())
                        .isAuthorizedUser(comment.getUser().equals(pharmacist))
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteComment(Integer commentId) {
        UserCommunityCommentEntity commentEntity = userCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        userCommunityCommentRepository.delete(commentEntity);
    }
}
