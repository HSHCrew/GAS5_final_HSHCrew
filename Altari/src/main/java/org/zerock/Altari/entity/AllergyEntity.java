package org.zerock.Altari.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "allergy")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AllergyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer allergy_id;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;
    @ManyToOne
    @JoinColumn(name = "medication_id")
    private MedicationEntity medication_id;
    @CreatedDate
    private LocalDateTime allergy_created_at;
    @LastModifiedDate
    private LocalDateTime allergy_updated_at;

}
