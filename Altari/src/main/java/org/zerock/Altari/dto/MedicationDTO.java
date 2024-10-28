package org.zerock.Altari.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MedicationDTO {
    private Integer medication_id;
    private String medication_name;
    private String medication_info;
    private String interaction_info;
}
