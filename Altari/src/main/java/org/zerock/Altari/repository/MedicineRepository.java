package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.MedicineEntity;

public interface MedicineRepository extends JpaRepository<MedicineEntity, Integer> {
}
