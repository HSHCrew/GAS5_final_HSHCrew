package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserCommunityPostEntity;
import org.zerock.Altari.entity.UserMedicationTimeEntity;

import java.util.Optional;

@Repository
public interface UserCommunityPostRepository extends JpaRepository<UserCommunityPostEntity, Integer> {
    Optional<UserCommunityPostEntity> findByUserCommunityPostId(Integer postId);
}
