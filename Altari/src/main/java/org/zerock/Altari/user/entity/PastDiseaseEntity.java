package org.zerock.Altari.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "past_disease")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PastDiseaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int past_disease_id;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserEntity user_profile_id;
    @ManyToOne
    @JoinColumn(name = "disease_id")
    private DiseaseEntity disease_id;




}
