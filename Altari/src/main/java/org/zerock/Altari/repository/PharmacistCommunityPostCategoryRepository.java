package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.PharmacistCommunityPostCategoryEntity;
import org.zerock.Altari.entity.UserCommunityPostCategoryEntity;

import java.util.Optional;

public interface PharmacistCommunityPostCategoryRepository extends JpaRepository<PharmacistCommunityPostCategoryEntity, Integer> {
    Optional<PharmacistCommunityPostCategoryEntity> findByPharmacistCommunityPostCategoryId(Integer pharmacistCommunityPostCategoryId);
}
