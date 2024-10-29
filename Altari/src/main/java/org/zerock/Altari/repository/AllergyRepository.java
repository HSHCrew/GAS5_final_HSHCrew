package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.AllergyEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<AllergyEntity, Integer> {
    List<AllergyEntity> findByUserProfile(UserProfileEntity profile_id);
}
