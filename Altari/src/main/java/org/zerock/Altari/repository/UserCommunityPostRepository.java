package org.zerock.Altari.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserCommunityPostCategoryEntity;
import org.zerock.Altari.entity.UserCommunityPostEntity;
import org.zerock.Altari.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCommunityPostRepository extends JpaRepository<UserCommunityPostEntity, Integer> {
    Optional<UserCommunityPostEntity> findByUserCommunityPostId(Integer postId);
    Optional<Page<UserCommunityPostEntity>> findByUserCommunityPostCategoryAndIsDraftFalse(UserCommunityPostCategoryEntity category, Pageable pageable);
    Optional<Page<UserCommunityPostEntity>> findByUserCommunityPostCreatedAtAfterAndIsDraftFalse(LocalDateTime oneDayAgo, Pageable pageable);
    Optional<Page<UserCommunityPostEntity>> findByUserAndIsDraftFalse(UserEntity user, Pageable pageable);
    Optional<Page<UserCommunityPostEntity>> findByIsDraftFalse(Pageable pageable);
    Optional<List<UserCommunityPostEntity>> findByUserAndIsDraftTrue(UserEntity user);
    Optional<Page<UserCommunityPostEntity>> findByUserCommunityPostTitleAndIsDraftFalse(String keyword, Pageable pageable);
    Optional<Page<UserCommunityPostEntity>> findByUserCommunityPostTitleContainingAndUserCommunityPostCategoryUserCommunityPostCategoryIdAndIsDraftFalse(
            String keyword, Integer categoryId, Pageable pageable);
}
