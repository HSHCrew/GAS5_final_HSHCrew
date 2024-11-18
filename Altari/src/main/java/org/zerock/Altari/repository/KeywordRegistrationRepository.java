package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.KeywordRegistrationEntity;
@Repository
public interface KeywordRegistrationRepository extends JpaRepository<KeywordRegistrationEntity, Integer> {
}


