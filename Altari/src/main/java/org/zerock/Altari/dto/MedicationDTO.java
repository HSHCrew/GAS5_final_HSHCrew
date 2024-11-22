package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MedicationDTO {


    private Integer medicationId;
    private String itemSeq; // 품목기준코드
    private String resultCode; // 결과코드
    private String resultMsg; // 결과메시지
    private Integer numOfRows; // 한 페이지 결과 수
    private Integer pageNo; // 페이지 번호
    private Integer totalCount; // 전체 결과 수
    private String entpName; // 업체명
    private String medicationName; // 제품명
    private String medicationInfo; // 효능 문항
    private String useMethodQesitm; // 사용법 문항
    private String atpnWarnQesitm; // 주의사항 경고 문항
    private String atpnQesitm; // 주의사항 문항
    private String interactionInfo; // 상호작용 문항
    private String seQesitm; // 부작용 문항
    private String depositMethodQesitm; // 보관법 문항
    private String openDe; // 공개일자
    private String updateDe; // 수정일자
    private String itemImage; // 약물 이미지 URL
    private LocalDateTime drug_created_at;
    private LocalDateTime drug_updated_at;


}
