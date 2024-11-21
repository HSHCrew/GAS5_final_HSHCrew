package org.zerock.Altari.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_medication_time")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserMedicationTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_medication_time_id")
    private Integer UserMedicationTimeId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity userProfile;

    @Column(name = "on_morning_medication_Alarm")
    private Boolean onMorningMedicationAlarm;

    @Column(name = "on_lunch_medication_time_Alarm")
    private Boolean onLunchMedicationTimeAlarm;

    @Column(name = "on_dinner_medication_time_Alarm")
    private Boolean onDinnerMedicationTimeAlarm;

    @Column(name = "on_night_medication_time_Alarm")
    private Boolean onNightMedicationTimeAlarm;

}
