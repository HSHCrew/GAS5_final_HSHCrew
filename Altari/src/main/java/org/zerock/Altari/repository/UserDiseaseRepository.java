package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.UserDiseaseEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.util.List;

@Repository
public interface UserDiseaseRepository extends JpaRepository<UserDiseaseEntity, Integer> {
    List<UserDiseaseEntity> findByUserProfile(UserProfileEntity profile_id);
}

