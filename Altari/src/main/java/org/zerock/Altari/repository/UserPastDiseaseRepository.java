package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserPastDiseaseEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPastDiseaseRepository extends JpaRepository<UserPastDiseaseEntity, Integer> {
    Optional<List<UserPastDiseaseEntity>> findByUser(UserEntity user);
}
