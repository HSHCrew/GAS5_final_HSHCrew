package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserPastDiseaseEntity;
@Repository
public interface PastDiseaseRepository extends JpaRepository<UserPastDiseaseEntity, Integer> {
}
