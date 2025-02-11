package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.FamilyGroupEntity;
import org.zerock.Altari.entity.FamilyMemberEntity;
import org.zerock.Altari.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMemberEntity, Integer> {
    Optional<List<FamilyMemberEntity>> findByUser(UserEntity user);
    Optional<List<FamilyMemberEntity>> findDistinctByUser(UserEntity user);
    Optional<FamilyMemberEntity> findByUserAndFamilyGroup(UserEntity user, FamilyGroupEntity familyGroup);
}
