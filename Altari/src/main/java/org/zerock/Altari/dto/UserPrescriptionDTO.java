package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.PrescriptionDrugEntity;
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


    private Integer user_prescription_id;
    private String prescribe_no;
    private String prescribe_org;
    private String comm_brand_name;
    private LocalDate manufacture_date;
    private String tel_no;
    private String tel_no1;
    private UserProfileEntity user_profile_id;
    private LocalDateTime user_prescription_created_at;
    private LocalDateTime user_prescription_updated_at;
    private List<PrescriptionDrugDTO> drugs;
    private String prescription_info;
    private String ai_summary;
    private String adherence_score;
}

