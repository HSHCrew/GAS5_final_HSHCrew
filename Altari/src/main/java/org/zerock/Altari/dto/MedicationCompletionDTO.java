package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class MedicationCompletionDTO {

    private Boolean morningTaken; // 아침 복약 여부
    private Boolean lunchTaken; // 점심 복약 여부
    private Boolean dinnerTaken; // 저녁 복약 여부
    private Boolean nightTaken; // 밤 복약 여부
}
