package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.PharmacistCommunityCommentEntity;
import org.zerock.Altari.entity.PharmacistCommunityPostEntity;
import org.zerock.Altari.entity.UserCommunityCommentEntity;
import org.zerock.Altari.entity.UserCommunityPostEntity;

import java.util.List;
import java.util.Optional;

public interface PharmacistCommunityCommentRepository extends JpaRepository<PharmacistCommunityCommentEntity, Integer> {

    Optional<List<PharmacistCommunityCommentEntity>> findByPharmacistCommunityPost(PharmacistCommunityPostEntity postEntity);
}
