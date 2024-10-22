package org.zerock.Altari.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String user_profile_id;

    private String full_name;

    private String nickname;

    private LocalDate date_of_birth;

    private String phone_number;

    private int height;

    private int weight;

    private String blood_type;

    private LocalTime morning_medication_time;

    private LocalTime lunch_medication_time;

    private LocalTime dinner_medication_time;


    @CreatedDate
    private LocalDateTime user_create_at;

    @LastModifiedDate
    private LocalDateTime user_update_at;

    public UserProfileEntity toEntity() {
        return UserProfileEntity.builder()
                .user_id(this.user_id)
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



