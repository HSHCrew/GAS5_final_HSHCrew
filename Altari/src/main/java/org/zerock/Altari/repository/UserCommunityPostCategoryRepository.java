package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.UserCommunityPostCategoryEntity;

import java.util.Optional;

public interface UserCommunityPostCategoryRepository extends JpaRepository<UserCommunityPostCategoryEntity, Integer> {
    Optional<UserCommunityPostCategoryEntity> findByUserCommunityPostCategoryId(Integer userCommunityPostCategory);
}
