package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Integer> {
    UserProfileEntity findByUsername(UserEntity username);
}
