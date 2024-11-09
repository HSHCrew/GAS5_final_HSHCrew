package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_prescription")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserPrescriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_prescription_id")
    private Integer userPrescriptionId;

    @Column(name = "prescribe_no",unique = true)
    private String prescribeNo;

    @Column(name = "prescribe_org")
    private String prescribeOrg;

    @Column(name = "comm_brand_name")
    private String commBrandName;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "tel_no")
    private String telNo;

    @Column(name = "tel_no1")
    private String telNo2;

    @Column(name = "prescription_info")
    private String prescriptionInfo;

    @Column(name = "ai_summary")
    private String aiSummary;

    @Column(name = "is_taken")
    private Boolean isTaken;

    @Column(name = "on_alarm")
    private Boolean onAlarm;

    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;

    @CreatedDate
    private LocalDateTime user_prescription_created_at;
    @LastModifiedDate
    private LocalDateTime user_prescription_updated_at;


}
