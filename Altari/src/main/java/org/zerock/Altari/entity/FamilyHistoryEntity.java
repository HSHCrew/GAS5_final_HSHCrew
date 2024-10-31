package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_history")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FamilyHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer family_history_id;
    @ManyToOne
    @JoinColumn(name = "disease_id")
    private DiseaseEntity disease;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;

    private String family_relation;
    @CreatedDate
    private LocalDateTime family_history_created_at;
    @LastModifiedDate
    private LocalDateTime family_history_updated_at;



}
