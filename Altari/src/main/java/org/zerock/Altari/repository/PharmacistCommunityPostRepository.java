package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.PharmacistCommunityPostEntity;

@Repository
public interface PharmacistCommunityPostRepository extends JpaRepository<PharmacistCommunityPostEntity, Integer> {
}
