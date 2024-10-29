package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserPastDiseaseEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.util.List;

@Repository
public interface UserPastDiseaseRepository extends JpaRepository<UserPastDiseaseEntity, Integer> {
    List<UserPastDiseaseEntity> findByUserProfile(UserProfileEntity profile_id);
}
