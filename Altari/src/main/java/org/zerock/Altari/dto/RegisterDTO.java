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
    private String username;
    private String password;
    private String role;
    private LocalDateTime user_created_at;
    private LocalDateTime user_updated_at;
}
