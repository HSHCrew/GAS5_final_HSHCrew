package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.FamilyGroupEntity;
import org.zerock.Altari.entity.UserEntity;

import java.util.Optional;

@Repository
public interface FamilyGroupRepository extends JpaRepository<FamilyGroupEntity, Integer> {
    Optional<FamilyGroupEntity> findByFamilyGroupId(Integer familyGroupId);
}
