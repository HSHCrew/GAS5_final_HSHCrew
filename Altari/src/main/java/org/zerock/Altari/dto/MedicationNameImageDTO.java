package org.zerock.Altari.dto;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MedicationNameImageDTO {

    private Integer medicationId;
    private String medicationName; // 제품명
    private String itemImage; // 약물 이미지 URL
    private String oneDose;
}
