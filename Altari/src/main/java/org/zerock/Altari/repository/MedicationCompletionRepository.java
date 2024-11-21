package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.MedicationCompletionEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.util.List;

public interface MedicationCompletionRepository extends JpaRepository<MedicationCompletionEntity, Integer> {
    List<MedicationCompletionEntity> findByUserProfile(UserProfileEntity userProfile);
}
