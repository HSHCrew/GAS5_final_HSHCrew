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

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "medication_id")
    private MedicationEntity medication;

    @Column(name = "news_content", columnDefinition = "TEXT")
    private String newsContent;
    @CreatedDate
    private LocalDateTime news_curation_created_at;

    @LastModifiedDate
    private LocalDateTime news_curation_updated_at;


}
