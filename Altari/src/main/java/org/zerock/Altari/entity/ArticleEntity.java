package org.zerock.Altari.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "article")
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer article_id; // Primary Key

    @Column(columnDefinition = "TEXT")
    private String topic; // 주제

    @Column(columnDefinition = "TEXT")
    private String title; // 제목

    @Column(columnDefinition = "TEXT")
    private String content; // 내용

    @Column()
    private LocalDate date; // 날짜 (LocalDate 타입 사용)

    @Column(columnDefinition = "TEXT")
    private String link; // 링크

    @Column(columnDefinition = "TEXT")
    private String imageUrl; // 이미지 URL
}