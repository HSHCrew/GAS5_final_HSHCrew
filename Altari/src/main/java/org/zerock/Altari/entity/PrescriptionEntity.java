package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescription")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PrescriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer prescription_id;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity user_profile_id;

    private String prescription_info;
    private String ai_summary;
    private String adherence_score;
    @CreatedDate
    private LocalDateTime prescription_created_at;
    @LastModifiedDate
    private LocalDateTime prescription_updated_at0;

}
