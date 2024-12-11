package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserMedicationEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMedicationRepository extends JpaRepository<UserMedicationEntity, Integer> {
    Optional<List<UserMedicationEntity>> findByPrescriptionId(UserPrescriptionEntity userPrescriptionId);
}
