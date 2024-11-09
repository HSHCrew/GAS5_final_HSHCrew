package org.zerock.Altari.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)

public class UserProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profile_id")
    private int userProfileId;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private UserEntity username;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone_number")
    private String phoneNumber;

    private Float height;

    private Float weight;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "morning_medication_time")
    private LocalTime morningMedicationTime;

    @Column(name = "lunch_medication_time")
    private LocalTime lunchMedicationTime;

    @Column(name = "dinner_medication_time")
    private LocalTime dinnerMedicationTime;


    @CreatedDate
    private LocalDateTime user_profile_created_at;

    @LastModifiedDate
    private LocalDateTime user_profile_updated_at;

    // 1. Allergy 테이블 (user_profile이 참조하는 allergy 테이블)
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<AllergyEntity> allergies = new ArrayList<>();

    // 2. Family History 테이블 (user_profile이 참조하는 family_history 테이블)
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<FamilyHistoryEntity> familyHistories = new ArrayList<>();

    // 3. Keyword Registration 테이블 (user_profile이 참조하는 keyword_registration 테이블)
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<KeywordRegistrationEntity> keywordRegistrations = new ArrayList<>();


    // 6. User Disease 테이블 (user_profile이 참조하는 user_disease 테이블)
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<UserDiseaseEntity> userDiseases = new ArrayList<>();

    // 7. User Past Disease 테이블 (user_profile이 참조하는 user_past_disease 테이블)
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<UserPastDiseaseEntity> userPastDiseases = new ArrayList<>();

    // 8. User Medication Info 테이블 (user_profile이 참조하는 user_medication_info 테이블)
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<UserMedicationInfoEntity> userMedicationInfos = new ArrayList<>();

    // 9. User Prescription 테이블 (user_profile이 참조하는 user_prescription 테이블)
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<UserPrescriptionEntity> userPrescriptions = new ArrayList<>();

}



