package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserDiseaseEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.util.List;

@Repository
public interface UserPrescriptionRepository extends JpaRepository<UserPrescriptionEntity, Integer> {
    UserPrescriptionEntity findByUserProfile(UserProfileEntity profile_id);
}
