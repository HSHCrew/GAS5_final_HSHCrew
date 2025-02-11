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
    private Integer userProfileId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "full_name", columnDefinition = "TEXT")
    private String fullName;

    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

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


}



