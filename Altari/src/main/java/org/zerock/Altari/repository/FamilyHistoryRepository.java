package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.FamilyHistoryEntity;
import org.zerock.Altari.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyHistoryRepository extends JpaRepository<FamilyHistoryEntity, Integer> {
    Optional<List<FamilyHistoryEntity>> findByUser(UserEntity user);
}
