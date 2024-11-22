package org.zerock.Altari.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column(name = "allergy_id")
    private Integer allergyId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_profile_id")
    @JsonIgnore
    private UserProfileEntity userProfile;

    @ManyToOne
    @JoinColumn(name = "medication_id", referencedColumnName = "medication_id")
    private MedicationEntity medication;

    @CreatedDate
    private LocalDateTime allergy_created_at;

    @LastModifiedDate
    private LocalDateTime allergy_updated_at;

}
