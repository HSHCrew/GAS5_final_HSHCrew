package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.MedicineEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserPrescriptionDTO {


    private int user_prescription_id;
    private String prescribe_no;
    private String prescribe_org;
    private String comm_brand_name;
    private LocalDate manufacture_date;
    private String tel_no;
    private String tel_no1;
    private UserProfileEntity user_profile_id;
    private LocalDateTime user_prescription_created_at;
    private LocalDateTime user_prescription_updated_at;
    private List<MedicineEntity> medicines;
}

