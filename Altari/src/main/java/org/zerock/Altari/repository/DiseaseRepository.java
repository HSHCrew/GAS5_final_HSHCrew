package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.DiseaseEntity;

import java.util.Optional;

@Repository
public interface DiseaseRepository extends JpaRepository<DiseaseEntity, Integer> {
    Optional<DiseaseEntity> findByDiseaseId(int diseaseId);
}
