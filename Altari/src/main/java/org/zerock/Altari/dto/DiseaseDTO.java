package org.zerock.Altari.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DiseaseDTO {
    private Integer diseaseId;
    private String diseaseCode;
    private String diseaseName;
    private LocalDateTime disease_created_at;
    private LocalDateTime disease_updated_at;
    private Boolean isHereditary;


}

