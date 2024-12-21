package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ArticleDTO {

    private Integer article_id; // Primary Key
    private String topic; // 주제
    private String title; // 제목
    private String content; // 내용
    private String date; // 날짜 (LocalDate 타입 사용)
    private String link; // 링크
    private String imageUrl; // 이미지 URL
    private String diseaseName;
}
