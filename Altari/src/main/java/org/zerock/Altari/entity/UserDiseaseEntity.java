package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_disease")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserDiseaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int user_disease_id;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity user_profile_id;
    @ManyToOne
    @JoinColumn(name = "disease_id")
    private DiseaseEntity disease_id;
    @CreatedDate
    private LocalDateTime user_disease_created_at;
    @LastModifiedDate
    private LocalDateTime user_disease_updated_at;

}
