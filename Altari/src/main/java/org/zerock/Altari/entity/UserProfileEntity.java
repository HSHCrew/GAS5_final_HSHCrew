package org.zerock.Altari.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

    private int user_profile_id;

    @OneToOne
    @JoinColumn(name = "username", referencedColumnName = "username")
    @JsonManagedReference
    private UserEntity username;

    private String full_name;

    private LocalDate date_of_birth;

    private String phone_number;
    @Column(nullable = true)
    private Integer height;

    private Integer weight;

    private String blood_type;

    private LocalTime morning_medication_time;

    private LocalTime lunch_medication_time;

    private LocalTime dinner_medication_time;


    @CreatedDate
    private LocalDateTime user_profile_created_at;

    @LastModifiedDate
    private LocalDateTime user_profile_updated_at;



}



