package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.DiseaseEntity;

public interface DiseaseRepository extends JpaRepository<DiseaseEntity, Integer> {
}
