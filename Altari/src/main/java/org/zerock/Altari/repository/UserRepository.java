package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {
}
