package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserPrescriptionEntity;

@Repository
public interface UserPrescriptionRepository extends JpaRepository<UserPrescriptionEntity, Integer> {
}
