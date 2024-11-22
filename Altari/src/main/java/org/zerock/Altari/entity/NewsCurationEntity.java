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
    @Column(name = "news_curation_id")
    private Integer newsCurationId;

    @Column(name = "keyword", columnDefinition = "TEXT")
    private String keyword;

    @Column(name = "curation_content", columnDefinition = "TEXT")
    private String curationContent;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private MedicationEntity article;

    @CreatedDate
    private LocalDateTime news_curation_created_at;

    @LastModifiedDate
    private LocalDateTime news_curation_updated_at;


}
