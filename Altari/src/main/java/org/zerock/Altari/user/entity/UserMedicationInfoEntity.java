package org.zerock.Altari.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_medication_info")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserMedicationInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int user_medication_info_id;
    @ManyToOne
    @JoinColumn(name = "prescription_id")
    private PrescriptionEntity prescription_id;
    @ManyToOne
    @JoinColumn(name = "medication_id")
    private ModicationEntity medication_id;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserEntity user_profile_id;
    private String user_medication_status;
    private String user_medication_datetime;
    private String dosage;

}