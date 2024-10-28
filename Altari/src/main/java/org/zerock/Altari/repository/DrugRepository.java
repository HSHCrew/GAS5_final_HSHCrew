package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.DrugEntity;
@Repository
public interface DrugRepository extends JpaRepository<DrugEntity, Integer> {
    DrugEntity findByItemseq(String itemSeq);
}
