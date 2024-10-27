package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.DrugEntity;

public interface DrugRepository extends JpaRepository<DrugEntity, Integer> {
    DrugEntity findByItemseq(String itemSeq);
}
