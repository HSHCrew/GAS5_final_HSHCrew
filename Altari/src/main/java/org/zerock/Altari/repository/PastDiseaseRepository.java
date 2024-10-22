package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.PastDiseaseEntity;

public interface PastDiseaseRepository extends JpaRepository<PastDiseaseEntity, Integer> {
}
