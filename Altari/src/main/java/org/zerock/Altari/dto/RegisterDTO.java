package org.zerock.Altari.dto;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegisterDTO {
    // user 테이블
    private String username;
    private String password;
    private String role;
    private LocalDateTime user_created_at;
    private LocalDateTime user_updated_at;
    // user_profile 테이블
    private int userProfileId;
    private UserEntity user;
    private String fullName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private Float height;
    private Float weight;
    private String bloodType;
    private LocalTime morningMedicationTime;
    private LocalTime lunchMedicationTime;
    private LocalTime dinnerMedicationTime;
    private LocalDateTime user_profile_created_at;
    private LocalDateTime user_profile_updated_at;
    private String medication_name;
    private DiseaseEntity disease_id;
    private String family_relation;
    private MedicationEntity medication_id;

}
