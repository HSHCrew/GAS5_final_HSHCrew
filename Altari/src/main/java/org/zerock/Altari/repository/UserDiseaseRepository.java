package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserDiseaseEntity;
import org.zerock.Altari.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDiseaseRepository extends JpaRepository<UserDiseaseEntity, Integer> {
    Optional<List<UserDiseaseEntity>> findByUser(UserEntity user);
}

