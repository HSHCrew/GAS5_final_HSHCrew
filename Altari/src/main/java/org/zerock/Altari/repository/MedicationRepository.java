package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.MedicationEntity;

public interface MedicationRepository extends JpaRepository<MedicationEntity, Integer> {
}
