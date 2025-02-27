package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.NewsCurationEntity;
@Repository
public interface NewsCurationRepository extends JpaRepository<NewsCurationEntity, Integer>{
    NewsCurationEntity findByDisease(DiseaseEntity DiseaseId);
}

