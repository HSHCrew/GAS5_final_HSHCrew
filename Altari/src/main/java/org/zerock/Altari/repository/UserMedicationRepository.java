package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserMedicationEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;

import java.util.List;

@Repository
public interface UserMedicationRepository extends JpaRepository<UserMedicationEntity, Integer> {
    List<UserMedicationEntity> findByPrescriptionId(UserPrescriptionEntity userPrescriptionId);
}
