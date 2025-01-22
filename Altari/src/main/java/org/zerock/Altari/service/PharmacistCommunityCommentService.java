package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.PharmacistCommunityCommentDTO;
import org.zerock.Altari.dto.UserCommunityCommentDTO;
import org.zerock.Altari.entity.PharmacistCommunityCommentEntity;
import org.zerock.Altari.entity.PharmacistCommunityPostEntity;
import org.zerock.Altari.entity.UserCommunityCommentEntity;
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

    public PharmacistCommunityCommentDTO createReplyComment(UserEntity user, Integer parentCommentId, PharmacistCommunityCommentDTO commentDTO) {
        PharmacistCommunityCommentEntity parentCommentEntity = pharmacistCommunityCommentRepository.findById(parentCommentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        PharmacistCommunityCommentEntity maxGroupOrderComment = pharmacistCommunityCommentRepository.findTopByPharmacistCommunityCommentGroupIdOrderByPharmacistCommunityCommentGroupOrderDesc(parentCommentEntity.getPharmacistCommunityCommentGroupId())
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        PharmacistCommunityCommentEntity commentEntity = PharmacistCommunityCommentEntity.builder()
                .pharmacistCommunityPost(parentCommentEntity.getPharmacistCommunityPost())
                .user(user)
                .pharmacistCommunityCommentGroupId(parentCommentEntity.getPharmacistCommunityCommentGroupId())
                .pharmacistCommunityCommentGroupOrder(maxGroupOrderComment.getPharmacistCommunityCommentGroupOrder() + 1) // 최대 그룹 순서 + 1
                .pharmacistCommunityCommentDepth(1) // Depth는 1로 고정
                .pharmacistCommunityCommentContent(commentDTO.getPharmacistCommunityCommentContent())
                .pharmacistCommunityCommentLikes(0)
                .build();

        PharmacistCommunityCommentEntity createdReplyComment = pharmacistCommunityCommentRepository.save(commentEntity);

        return PharmacistCommunityCommentDTO.builder()
                .pharmacistCommunityCommentId(createdReplyComment.getPharmacistCommunityCommentId())
                .pharmacistCommunityPost(createdReplyComment.getPharmacistCommunityPost().getPharmacistCommunityPostId())
                .user(user.getUsername())
                .pharmacistCommunityCommentGroupId(createdReplyComment.getPharmacistCommunityCommentGroupId())
                .pharmacistCommunityCommentGroupOrder(createdReplyComment.getPharmacistCommunityCommentGroupOrder())
                .pharmacistCommunityCommentDepth(createdReplyComment.getPharmacistCommunityCommentDepth())
                .pharmacistCommunityCommentContent(createdReplyComment.getPharmacistCommunityCommentContent())
                .pharmacistCommunityCommentLikes(createdReplyComment.getPharmacistCommunityCommentLikes())
                .pharmacistCommunityCommentCreatedAt(createdReplyComment.getPharmacistCommunityCommentCreatedAt())
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

    // 댓글 좋아요 추가
    public PharmacistCommunityCommentDTO likeComment(Integer commentId) {
        PharmacistCommunityCommentEntity commentEntity = pharmacistCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 좋아요 수 증가
        commentEntity.setPharmacistCommunityCommentLikes(commentEntity.getPharmacistCommunityCommentLikes() + 1);

        // 저장 및 DTO 반환
        pharmacistCommunityCommentRepository.save(commentEntity);
        return PharmacistCommunityCommentDTO.builder()
                .pharmacistCommunityCommentId(commentEntity.getPharmacistCommunityCommentId())
                .pharmacistCommunityCommentLikes(commentEntity.getPharmacistCommunityCommentLikes())
                .build();
    }

    // 댓글 좋아요 취소
    public PharmacistCommunityCommentDTO unlikeComment(Integer commentId) {
        PharmacistCommunityCommentEntity commentEntity = pharmacistCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 좋아요 수 감소 (0 이하로는 감소하지 않도록 처리)
        int updatedLikes = Math.max(commentEntity.getPharmacistCommunityCommentLikes() - 1, 0);
        commentEntity.setPharmacistCommunityCommentLikes(updatedLikes);

        // 저장 및 DTO 반환
        pharmacistCommunityCommentRepository.save(commentEntity);

        return PharmacistCommunityCommentDTO.builder()
                .pharmacistCommunityCommentId(commentEntity.getPharmacistCommunityCommentId())
                .pharmacistCommunityCommentLikes(commentEntity.getPharmacistCommunityCommentLikes())
                .build();
    }

    public void deleteComment(Integer commentId) {
        PharmacistCommunityCommentEntity commentEntity = pharmacistCommunityCommentRepository.findById(commentId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        pharmacistCommunityCommentRepository.delete(commentEntity);
    }
}
