package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.AllergyEntity;

public interface AllergyRepository extends JpaRepository<AllergyEntity, Integer> {
}
