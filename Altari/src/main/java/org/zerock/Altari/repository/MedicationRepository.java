package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.MedicationEntity;
@Repository
public interface MedicationRepository extends JpaRepository<MedicationEntity, Integer> {
}
