package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.Altari.entity.ChatSummaryEntity;
@Repository
public interface ChatSummaryRepository extends JpaRepository<ChatSummaryEntity, Integer> {
}
