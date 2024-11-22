package org.zerock.Altari.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDTO {

    private Integer userProfileId;
    private String fullName;
    private String profileImage;
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
