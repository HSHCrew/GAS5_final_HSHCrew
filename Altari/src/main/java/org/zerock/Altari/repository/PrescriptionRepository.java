package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.PrescriptionEntity;

public interface PrescriptionRepository extends JpaRepository<PrescriptionEntity, Integer> {
}
