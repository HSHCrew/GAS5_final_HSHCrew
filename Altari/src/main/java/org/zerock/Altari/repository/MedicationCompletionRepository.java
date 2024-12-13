package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.MedicationCompletionEntity;
import org.zerock.Altari.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface MedicationCompletionRepository extends JpaRepository<MedicationCompletionEntity, Integer> {
    Optional<List<MedicationCompletionEntity>> findByUser(UserEntity user);
}
