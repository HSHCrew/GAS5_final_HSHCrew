package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPrescriptionRepository extends JpaRepository<UserPrescriptionEntity, Integer> {
    // UserPrescriptionRepository
    Optional<List<UserPrescriptionEntity>> findByUser(UserEntity user);


    Optional<UserPrescriptionEntity> findByPrescribeNo(String prescribeNo);

    Optional<List<UserPrescriptionEntity>> findByUserAndIsTakenFalse(UserEntity user);

    Optional<UserPrescriptionEntity> findByUserPrescriptionId(Integer userPrescriptionId);
}
