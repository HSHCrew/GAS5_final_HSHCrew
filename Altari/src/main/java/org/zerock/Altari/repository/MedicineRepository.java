package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.MedicineEntity;
import org.zerock.Altari.entity.UserPastDiseaseEntity;
import org.zerock.Altari.entity.UserProfileEntity;

@Repository
public interface MedicineRepository extends JpaRepository<MedicineEntity, Integer> {

}
