package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.UserDiseaseEntity;

public interface UserDiseaseRepository extends JpaRepository<UserDiseaseEntity, Integer> {
}
