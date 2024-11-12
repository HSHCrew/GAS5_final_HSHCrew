package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserProfileDTO {

    private Integer userProfileId;
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




}
