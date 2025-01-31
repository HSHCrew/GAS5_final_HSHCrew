package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medication_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MedicationSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_summary_id")
    private Integer MedicationSummaryId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "medication_id", nullable = false)
    private MedicationEntity medication;

    @Column(name = "restructured", columnDefinition = "TEXT")
    private String restructured;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "fewshots", columnDefinition = "TEXT")
    private String fewshots;

    @Column(name = "failed", columnDefinition = "TEXT")
    private String failed;

    @Column(name = "medication_summary_created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private LocalDateTime MedicationSummaryCreatedAt;

    @Column(name = "medication_summary_updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime MedicationSummaryUpdatedAt;
}