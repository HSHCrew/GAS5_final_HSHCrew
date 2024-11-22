package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.UserDiseaseEntity;

import java.util.List;

@Repository
public interface DiseaseRepository extends JpaRepository<DiseaseEntity, Integer> {
    DiseaseEntity findByDiseaseId(int diseaseId);
}
