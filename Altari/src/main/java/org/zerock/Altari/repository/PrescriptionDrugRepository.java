package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.PrescriptionDrugEntity;

@Repository
public interface PrescriptionDrugRepository extends JpaRepository<PrescriptionDrugEntity, Integer> {
}
