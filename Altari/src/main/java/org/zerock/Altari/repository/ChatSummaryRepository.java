package org.zerock.Altari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.entity.ChatSummaryEntity;

public interface ChatSummaryRepository extends JpaRepository<ChatSummaryEntity, Integer> {
}
