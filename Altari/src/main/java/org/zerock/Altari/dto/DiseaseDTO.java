package org.zerock.Altari.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DiseaseDTO {
    private Integer diseaseId;
    private String disease_name;
    private String disease_info;
    private Boolean is_hereditary;
}

