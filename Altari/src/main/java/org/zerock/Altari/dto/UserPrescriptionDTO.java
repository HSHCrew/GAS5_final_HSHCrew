package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
public class UserPrescriptionDTO {


    private Integer userPrescriptionId;
    private String prescriptionNo;
    private String prescriptionOrg;
    private String commBrandName;
    private LocalDate manufactureDate;
    private String telNo;
    private String telNo1;
    private UserProfileEntity userProfile;
    private LocalDateTime user_prescription_created_at;
    private LocalDateTime user_prescription_updated_at;
    private List<UserMedicationDTO> drugs;
    private String prescriptionInfo;
    private String aiSummary;
    private Boolean isTaken;
    private Boolean onAlarm;
    private Integer totalDosingDay;
}

