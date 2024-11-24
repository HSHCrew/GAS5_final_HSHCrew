package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.ArticleEntity;
import org.zerock.Altari.entity.ChatSummaryEntity;
import org.zerock.Altari.entity.DiseaseEntity;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, Integer> {
    List<ArticleEntity> findByDisease(DiseaseEntity DiseaseId);
}
