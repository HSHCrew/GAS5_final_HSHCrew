package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_curation")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class NewsCurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int news_curation_id;
    @ManyToOne
    @JoinColumn(name = "health_keyword_id")
    private HealthKeywordEntity health_keyword_id;
    @ManyToOne
    @JoinColumn(name = "medication_id")
    private ModicationEntity medication_id;

    private String news_content;
    @CreatedDate
    private LocalDateTime user_create_at;

    @LastModifiedDate
    private LocalDateTime user_update_at;


}
