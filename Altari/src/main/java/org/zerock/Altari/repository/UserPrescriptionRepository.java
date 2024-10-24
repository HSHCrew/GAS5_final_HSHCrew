package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.UserPrescriptionEntity;


public interface UserPrescriptionRepository extends JpaRepository<UserPrescriptionEntity, Integer> {
}
