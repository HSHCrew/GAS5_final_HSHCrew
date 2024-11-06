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
    private Integer user_prescription_id;

    @Column(name = "prescribe_no",unique = true)
    private String prescribeNo;
    private String prescribe_org;
    private String comm_brand_name;
    private LocalDate manufacture_date;
    private String tel_no;
    private String tel_no1;
    private String prescription_info;
    private String ai_summary;
    @Column(name = "is_taken")
    private Boolean isTaken;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;

    @CreatedDate
    private LocalDateTime user_prescription_created_at;
    @LastModifiedDate
    private LocalDateTime user_prescription_updated_at;

//    @OneToMany(mappedBy = "prescriptionId", cascade = CascadeType.ALL)
//    private List<PrescriptionDrugEntity> prescriptionDrugs = new ArrayList<>();


}
