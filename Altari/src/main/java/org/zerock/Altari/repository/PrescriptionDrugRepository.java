package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.PrescriptionDrugEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;

import java.util.List;

@Repository
public interface PrescriptionDrugRepository extends JpaRepository<PrescriptionDrugEntity, Integer> {
    List<PrescriptionDrugEntity> findByPrescriptionId(UserPrescriptionEntity userPrescriptionId);
}
