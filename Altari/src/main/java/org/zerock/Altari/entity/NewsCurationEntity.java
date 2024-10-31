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
    private Integer news_curation_id;
    @ManyToOne
    @JoinColumn(name = "health_keyword_id")
    private HealthKeywordEntity health_keyword_id;
    @ManyToOne
    @JoinColumn(name = "medication_id")
    private MedicationEntity medication_id;

    private String news_content;
    @CreatedDate
    private LocalDateTime news_curation_created_at;

    @LastModifiedDate
    private LocalDateTime news_curation_updated_at;


}
