package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.UserCommunityFileEntity;
import org.zerock.Altari.entity.UserCommunityPostEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCommunityFileRepository extends JpaRepository<UserCommunityFileEntity, Integer> {

    Optional<List<UserCommunityFileEntity>> findByUserCommunityPost(UserCommunityPostEntity userCommunityPost);

}
