package org.zerock.Altari.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegisterDTO {
    // user 테이블
    private String username;
    private String password;
    private String role;
    private LocalDateTime user_created_at;
    private LocalDateTime user_updated_at;
    // user_profile 테이블
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
    private int auth_id;
    // allergy 테이블
    private String food_name;
    // medication 테이블
    private String medication_name;
}
