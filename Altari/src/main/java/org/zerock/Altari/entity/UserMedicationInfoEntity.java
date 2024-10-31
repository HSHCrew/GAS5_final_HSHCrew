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
    private Integer user_medication_info_id;
    @ManyToOne
    @JoinColumn(name = "user_prescription_id")
    private UserPrescriptionEntity userPrescriptionId;
    @ManyToOne
    @JoinColumn(name = "item_seq")
    private MedicationEntity mediacationId;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;
    private String user_medication_status;
    private String user_medication_datetime;
    private String dosage;
    @CreatedDate
    private LocalDateTime user_medication_info_created_at;
    @LastModifiedDate
    private LocalDateTime user_medication_info_updated_at;


}