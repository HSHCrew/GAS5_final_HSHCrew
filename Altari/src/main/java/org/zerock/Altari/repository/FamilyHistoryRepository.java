package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.FamilyHistoryEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.util.List;

@Repository
public interface FamilyHistoryRepository extends JpaRepository<FamilyHistoryEntity, Integer> {
    List<FamilyHistoryEntity> findByUserProfile(UserProfileEntity profile_id);
}
