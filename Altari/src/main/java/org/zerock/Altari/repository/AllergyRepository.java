package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.AllergyEntity;
import org.zerock.Altari.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllergyRepository extends JpaRepository<AllergyEntity, Integer> {
    Optional<List<AllergyEntity>> findByUser(UserEntity user);
}
