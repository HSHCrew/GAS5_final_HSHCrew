package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.PrescriptionEntity;
@Repository
public interface PrescriptionRepository extends JpaRepository<PrescriptionEntity, Integer> {
}
