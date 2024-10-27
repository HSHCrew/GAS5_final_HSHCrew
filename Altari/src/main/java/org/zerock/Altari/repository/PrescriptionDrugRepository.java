package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.PrescriptionDrugEntity;

public interface PrescriptionDrugRepository extends JpaRepository<PrescriptionDrugEntity, Integer> {
}
