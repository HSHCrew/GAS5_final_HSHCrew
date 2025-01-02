package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserCommunityCommentEntity;
import org.zerock.Altari.entity.UserCommunityPostEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCommunityCommentRepository extends JpaRepository<UserCommunityCommentEntity, Integer> {
    Optional<UserCommunityCommentEntity> findByUserCommunityCommentId(Integer postId);

    Optional<List<UserCommunityCommentEntity>> findByUserCommunityPost(UserCommunityPostEntity postEntity);
}
