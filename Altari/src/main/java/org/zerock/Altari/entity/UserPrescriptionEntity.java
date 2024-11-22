package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

    @Column(name = "prescribe_no",unique = true, columnDefinition = "TEXT")
    private String prescribeNo;

    @Column(name = "prescribe_org", columnDefinition = "TEXT")
    private String prescribeOrg;

    @Column(name = "comm_brand_name", columnDefinition = "TEXT")
    private String commBrandName;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "tel_no")
    private String telNo;

    @Column(name = "tel_no1")
    private String telNo2;

    @Column(name = "prescription_info", columnDefinition = "TEXT")
    private String prescriptionInfo;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "is_taken")
    private Boolean isTaken;

    @Column(name = "on_alarm")
    private Boolean onAlarm;

    @Column(name = "total_dosing_day")
    private Integer totalDosingDay;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;

    @CreatedDate
    private LocalDateTime user_prescription_created_at;
    @LastModifiedDate
    private LocalDateTime user_prescription_updated_at;


}
