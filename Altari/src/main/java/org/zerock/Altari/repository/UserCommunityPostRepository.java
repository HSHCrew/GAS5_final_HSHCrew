package org.zerock.Altari.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserCommunityPostCategoryEntity;
import org.zerock.Altari.entity.UserCommunityPostEntity;
import org.zerock.Altari.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserCommunityPostRepository extends JpaRepository<UserCommunityPostEntity, Integer> {
    Optional<UserCommunityPostEntity> findByUserCommunityPostId(Integer postId);

    Optional<Page<UserCommunityPostEntity>> findByUserCommunityPostCategory(UserCommunityPostCategoryEntity category, Pageable pageable);
    Optional<Page<UserCommunityPostEntity>> findByUserCommunityPostCreatedAtAfter(LocalDateTime oneDayAgo, Pageable pageable);

    Optional<Page<UserCommunityPostEntity>> findByUser(UserEntity user, Pageable pageable);
}
