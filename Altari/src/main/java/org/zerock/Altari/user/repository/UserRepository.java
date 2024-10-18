package org.zerock.Altari.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {
}
