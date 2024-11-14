package org.zerock.Altari.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMedicationTimeDTO {

    private Boolean onMorningMedicationAlarm;
    private Boolean onLunchMedicationTimeAlarm;
    private Boolean onDinnerMedicationTimeAlarm;
    private Boolean onNightMedicationTimeAlarm;

    // 수동으로 is 메서드를 생성

    public Boolean isOnMorningMedicationAlarm() {
        return onMorningMedicationAlarm;
    }

    public Boolean isOnLunchMedicationTimeAlarm() {
        return onLunchMedicationTimeAlarm;
    }

    public Boolean isOnDinnerMedicationTimeAlarm() {
        return onDinnerMedicationTimeAlarm;
    }

    public Boolean isOnNightMedicationTimeAlarm() {
        return onNightMedicationTimeAlarm;
    }
}