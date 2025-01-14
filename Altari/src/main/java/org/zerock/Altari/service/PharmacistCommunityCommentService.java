package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.PharmacistCommunityCommentDTO;
import org.zerock.Altari.entity.PharmacistCommunityCommentEntity;
import org.zerock.Altari.entity.PharmacistCommunityPostEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.PharmacistCommunityCommentRepository;
import org.zerock.Altari.repository.PharmacistCommunityPostRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class PharmacistCommunityCommentService {

    private final PharmacistCommunityCommentRepository pharmacistCommunityCommentRepository;
    private final PharmacistCommunityPostRepository pharmacistCommunityPostRepository;

    public PharmacistCommunityCommentDTO createComment(UserEntity user, Integer postId, PharmacistCommunityCommentDTO commentDTO) {
        PharmacistCommunityPostEntity postEntity = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        PharmacistCommunityCommentEntity commentEntity = PharmacistCommunityCommentEntity.builder()
                .pharmacistCommunityPost(postEntity)
                .user(user)
                .pharmacistCommunityCommentGroupId(null)
                .pharmacistCommunityCommentGroupOrder(0)
                .pharmacistCommunityCommentDepth(0)
                .pharmacistCommunityCommentContent(commentDTO.getPharmacistCommunityCommentContent())
                .pharmacistCommunityCommentLikes(0)
                .build();

        PharmacistCommunityCommentEntity createdComment = pharmacistCommunityCommentRepository.save(commentEntity);
        createdComment.setPharmacistCommunityCommentGroupId(createdComment.getPharmacistCommunityCommentId());

        return PharmacistCommunityCommentDTO.builder()
                .pharmacistCommunityCommentId(createdComment.getPharmacistCommunityCommentId())
                .pharmacistCommunityPost(postEntity.getPharmacistCommunityPostId())
                .user(user.getUsername())
                .pharmacistCommunityCommentGroupId(createdComment.getPharmacistCommunityCommentGroupId())
                .pharmacistCommunityCommentGroupOrder(createdComment.getPharmacistCommunityCommentGroupOrder())
                .pharmacistCommunityCommentDepth(createdComment.getPharmacistCommunityCommentDepth())
                .pharmacistCommunityCommentContent(createdComment.getPharmacistCommunityCommentContent())
                .pharmacistCommunityCommentLikes(createdComment.getPharmacistCommunityCommentLikes())
                .pharmacistCommunityCommentCreatedAt(createdComment.getPharmacistCommunityCommentCreatedAt())
                .build();
    }

    public PharmacistCommunityCommentDTO updateComment(Integer commentId, PharmacistCommunityCommentDTO commentDTO) {
        PharmacistCommunityCommentEntity commentEntity = pharmacistCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        commentEntity.setPharmacistCommunityCommentContent(commentDTO.getPharmacistCommunityCommentContent());

        PharmacistCommunityCommentEntity updatedComment = pharmacistCommunityCommentRepository.save(commentEntity);

        return PharmacistCommunityCommentDTO.builder()
                .pharmacistCommunityCommentId(updatedComment.getPharmacistCommunityCommentId())
                .pharmacistCommunityPost(updatedComment.getPharmacistCommunityPost().getPharmacistCommunityPostId())
                .user(updatedComment.getUser().getUsername())
                .pharmacistCommunityCommentGroupId(updatedComment.getPharmacistCommunityCommentGroupId())
                .pharmacistCommunityCommentGroupOrder(updatedComment.getPharmacistCommunityCommentGroupOrder())
                .pharmacistCommunityCommentDepth(updatedComment.getPharmacistCommunityCommentDepth())
                .pharmacistCommunityCommentContent(updatedComment.getPharmacistCommunityCommentContent())
                .pharmacistCommunityCommentLikes(updatedComment.getPharmacistCommunityCommentLikes())
                .pharmacistCommunityCommentCreatedAt(updatedComment.getPharmacistCommunityCommentCreatedAt())
                .pharmacistCommunityCommentUpdatedAt(updatedComment.getPharmacistCommunityCommentUpdatedAt())
                .build();
    }

    public PharmacistCommunityCommentDTO readComment(UserEntity user, Integer commentId) {

        PharmacistCommunityCommentEntity commentEntity = pharmacistCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        return PharmacistCommunityCommentDTO.builder()
                .pharmacistCommunityCommentId(commentEntity.getPharmacistCommunityCommentId())
                .pharmacistCommunityPost(commentEntity.getPharmacistCommunityPost().getPharmacistCommunityPostId())
                .user(commentEntity.getUser().getUsername())
                .pharmacistCommunityCommentGroupId(commentEntity.getPharmacistCommunityCommentGroupId())
                .pharmacistCommunityCommentGroupOrder(commentEntity.getPharmacistCommunityCommentGroupOrder())
                .pharmacistCommunityCommentDepth(commentEntity.getPharmacistCommunityCommentDepth())
                .pharmacistCommunityCommentContent(commentEntity.getPharmacistCommunityCommentContent())
                .pharmacistCommunityCommentLikes(commentEntity.getPharmacistCommunityCommentLikes())
                .pharmacistCommunityCommentCreatedAt(commentEntity.getPharmacistCommunityCommentCreatedAt())
                .pharmacistCommunityCommentUpdatedAt(commentEntity.getPharmacistCommunityCommentUpdatedAt())
                .isAuthorizedUser(commentEntity.getUser().equals(user))
                .build();
    }

    public List<PharmacistCommunityCommentDTO> readAllComments(UserEntity user, Integer postId) {
        PharmacistCommunityPostEntity postEntity = pharmacistCommunityPostRepository.findById(postId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        List<PharmacistCommunityCommentEntity> comments = pharmacistCommunityCommentRepository.findByPharmacistCommunityPost(postEntity)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        return comments.stream().map(comment -> PharmacistCommunityCommentDTO.builder()
                        .pharmacistCommunityCommentId(comment.getPharmacistCommunityCommentId())
                        .pharmacistCommunityPost(comment.getPharmacistCommunityPost().getPharmacistCommunityPostId())
                        .user(comment.getUser().getUsername())
                        .pharmacistCommunityCommentGroupId(comment.getPharmacistCommunityCommentGroupId())
                        .pharmacistCommunityCommentGroupOrder(comment.getPharmacistCommunityCommentGroupOrder())
                        .pharmacistCommunityCommentDepth(comment.getPharmacistCommunityCommentDepth())
                        .pharmacistCommunityCommentContent(comment.getPharmacistCommunityCommentContent())
                        .pharmacistCommunityCommentLikes(comment.getPharmacistCommunityCommentLikes())
                        .pharmacistCommunityCommentCreatedAt(comment.getPharmacistCommunityCommentCreatedAt())
                        .pharmacistCommunityCommentUpdatedAt(comment.getPharmacistCommunityCommentUpdatedAt())
                        .isAuthorizedUser(comment.getUser().equals(user))
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteComment(Integer commentId) {
        PharmacistCommunityCommentEntity commentEntity = pharmacistCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        pharmacistCommunityCommentRepository.delete(commentEntity);
    }
}
