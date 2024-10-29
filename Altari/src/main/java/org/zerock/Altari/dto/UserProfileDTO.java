package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserProfileDTO {

    private int user_profile_id;
    private String full_name;
    private LocalDate date_of_birth;
    private String phone_number;
    private int height;
    private int weight;
    private String blood_type;
    private LocalTime morning_medication_time;
    private LocalTime lunch_medication_time;
    private LocalTime dinner_medication_time;
    private LocalDateTime user_profile_created_at;
    private LocalDateTime user_profile_updated_at;

    public UserProfileDTO(UserProfileEntity userProfileEntity) {

        this.user_profile_id = userProfileEntity.getUser_profile_id();
        this.full_name = userProfileEntity.getFull_name();
        this.date_of_birth = userProfileEntity.getDate_of_birth();
        this.phone_number = userProfileEntity.getPhone_number();
        this.height = userProfileEntity.getHeight();
        this.weight = userProfileEntity.getWeight();
        this.blood_type = userProfileEntity.getBlood_type();
        this.morning_medication_time = userProfileEntity.getMorning_medication_time();
        this.lunch_medication_time = userProfileEntity.getLunch_medication_time();
        this.dinner_medication_time = userProfileEntity.getDinner_medication_time();



    }

    public UserProfileEntity toEntity() {
        return UserProfileEntity.builder()
                .full_name(this.full_name)
                .date_of_birth(this.date_of_birth)
                .phone_number(this.phone_number)
                .height(this.height)
                .weight(this.weight)
                .blood_type(this.blood_type)
                .morning_medication_time(this.morning_medication_time)
                .lunch_medication_time(this.lunch_medication_time)
                .dinner_medication_time(this.dinner_medication_time)
                .build();
    }
}
