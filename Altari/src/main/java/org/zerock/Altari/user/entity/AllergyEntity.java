package org.zerock.Altari.user.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
    private int allergy_id;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserEntity user_profile_id;
    @ManyToOne
    @JoinColumn(name = "modication_id")
    private ModicationEntity modication_id;

    private String food_name;
}
