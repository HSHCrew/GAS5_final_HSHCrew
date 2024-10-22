package org.zerock.Altari.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DiseaseDTO {
    private int disease_id;
    private String disease_name;
    private String disease_info;
}
