package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Integer> {
    Optional<UserProfileEntity> findByUser(UserEntity user);
}
