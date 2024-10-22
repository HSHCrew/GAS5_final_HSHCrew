package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user")
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
    private UserEntity user_profile_id;
    @ManyToOne
    @JoinColumn(name = "disease_id")
    private DiseaseEntity disease_id;
}
