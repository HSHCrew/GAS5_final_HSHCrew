package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.UserPastDiseaseEntity;

public interface PastDiseaseRepository extends JpaRepository<UserPastDiseaseEntity, Integer> {
}
