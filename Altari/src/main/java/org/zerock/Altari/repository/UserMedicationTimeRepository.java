package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.UserMedicationTimeEntity;
import org.zerock.Altari.entity.UserPastDiseaseEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.util.List;

public interface UserMedicationTimeRepository extends JpaRepository<UserMedicationTimeEntity, Integer> {
    UserMedicationTimeEntity findByUserProfile(UserProfileEntity profile_id);
}
