package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    private String username;

    private String password;

    @CreatedDate
    private LocalDateTime user_created_at;
    @LastModifiedDate
    private LocalDateTime user_updated_at;

    @ManyToMany
    @JoinTable(
            name = "user_roles", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "user_id"), // 현재 엔티티의 외래키
            inverseJoinColumns = @JoinColumn(name = "role_id") // 반대 엔티티의 외래키
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_disease",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "disease_id")
    )
    private Set<DiseaseEntity> userDiseases = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_past_disease",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "disease_id")
    )
    private Set<DiseaseEntity> userPastDiseases = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "family_history",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "disease_id")
    )
    private Set<DiseaseEntity> familyHistories = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "allergy",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "medication_id")
    )
    private Set<MedicationEntity> allergies = new HashSet<>();

    public void changePassword(String password) {
        this.password = password;
    }

    public UserEntity(String username) {
        this.username = username;
        this.password = password;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<UserProfileEntity> userProfiles = new ArrayList<>();

    // 9. User Prescription 테이블 (user_profile이 참조하는 user_prescription 테이블)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<UserPrescriptionEntity> userPrescriptions = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<UserMedicationTimeEntity> userMedicationTimes = new ArrayList<>();

    // 2. Family History 테이블 (user_profile이 참조하는 family_history 테이블)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<UserMedicationEntity> userMedications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<MedicationCompletionEntity> medicationCompletions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<MedicationSummaryEntity> medicationSummaries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<ChatSummaryEntity> chatSummaries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<ExpertFeedbackEntity> expertFeedbackEntities = new ArrayList<>();



}




