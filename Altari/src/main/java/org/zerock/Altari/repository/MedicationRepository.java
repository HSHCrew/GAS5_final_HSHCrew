package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.MedicationEntity;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<MedicationEntity, String> {
    MedicationEntity findByMedicationName(String itemSeq);
    MedicationEntity findByMedicationId(MedicationEntity medication);
    List<MedicationEntity> findAllByMedicationNameIn(List<String> medicationNames);
}
