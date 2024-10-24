package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "medicine")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int medicine_id;
    private String drug_code;
    private String drug_name;
    private String ingredients;
    private String content;
    private String one_dose;
    private String daily_doses_number;
    private String total_dosing_days;
    private String prescribe_drug_effect;
    private String drug_image_link;
    private String brand;
    private String medication_direction;
    private String atc_code;
    private String formula;
    @CreatedDate
    private LocalDateTime medicine_created_at;
    @LastModifiedDate
    private LocalDateTime medicine_updated_at;

    @ManyToOne
    @JoinColumn(name = "user_prescription_id")
    private UserPrescriptionEntity user_prescription_id;


}
