package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserMedicationTimeEntity;

import java.util.Optional;

public interface UserMedicationTimeRepository extends JpaRepository<UserMedicationTimeEntity, Integer> {
    Optional<UserMedicationTimeEntity> findByUser(UserEntity user);
}
