package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private String prescribe_no;
    private String prescribe_org;
    private String comm_brand_name;
    private LocalDate manufacture_date;
    private String tel_no;
    private String tel_no1;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;

    @CreatedDate
    private LocalDateTime user_prescription_created_at;
    @LastModifiedDate
    private LocalDateTime user_prescription_updated_at;

    @OneToMany(mappedBy = "user_prescription_id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicineEntity> medicines;
}
