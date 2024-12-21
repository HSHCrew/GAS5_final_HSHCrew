package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.ArticleEntity;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.MedicationEntity;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewsCurationDTO {

    private Integer newsCurationId;
    private String keyword;
    private String curationContent;
    private String koreanCurationContent;
    private String generatedAt;
    private String disease;
    private List<ArticleDTO> articles;


}
