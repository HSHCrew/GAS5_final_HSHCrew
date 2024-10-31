package org.zerock.Altari.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
    private Integer medicationId; // 품목기준코드
    private String result_code; // 결과코드
    private String result_msg; // 결과메시지
    private Integer num_of_rows; // 한 페이지 결과 수
    private Integer page_no; // 페이지 번호
    private Integer total_count; // 전체 결과 수
    private String entp_name; // 업체명
    private String medication_name; // 제품명
    private String medication_info; // 효능 문항
    private String use_method_qesitm; // 사용법 문항
    private String atpn_warn_qesitm; // 주의사항 경고 문항
    private String atpn_qesitm; // 주의사항 문항
    private String interaction_info; // 상호작용 문항
    private String se_qesitm; // 부작용 문항
    private String deposit_method_qesitm; // 보관법 문항
    private String open_de; // 공개일자
    private String update_de; // 수정일자
    private String item_image; // 약물 이미지 URL
    private LocalDateTime drug_created_at;
    private LocalDateTime drug_updated_at;

}
