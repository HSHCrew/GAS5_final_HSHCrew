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
    @Column(name = "user_medication_info_id")
    private Integer userMedicationInfoId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_prescription_id")
    private UserPrescriptionEntity userPrescriptionId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "item_seq")
    private MedicationEntity mediacationId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;

    @Column(name = "user_medication_status")
    private String userMedicationStatus;

    @Column(name = "user_medication_datetime")
    private String userMedicationDatetime;

    private String dosage;

    @CreatedDate
    private LocalDateTime user_medication_info_created_at;

    @LastModifiedDate
    private LocalDateTime user_medication_info_updated_at;


}