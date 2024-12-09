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
    private String diseaseName;  // 질환명
    private String classificationCode;  // 상병코드
    private String classification;  // 분류
    private String diseaseDefinition;  // 정의
    private String cause;  // 원인
    private String symptom;  // 증상
    private String treatment;  // 치료
    private String etcInfo;  // 기타
    private String attention;  // 복약 주의사항
    private String medicationAttention;  // 복약 주의사항
    private String lifeAttention;  // 일상생활 주의점
    private LocalDateTime diseaseCreatedAt;
    private LocalDateTime diseaseUpdatedAt;
    private Boolean isHereditary;  // 유전 여부


}

