package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.HealthKeywordEntity;
@Repository
public interface HealthKeywordRepository extends JpaRepository<HealthKeywordEntity, Integer> {
}
