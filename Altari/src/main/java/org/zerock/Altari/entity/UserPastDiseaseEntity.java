package org.zerock.Altari.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_past_disease")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserPastDiseaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_past_disease_id")
    private Integer userPastDiseaseId;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    @JsonIgnore
    private UserProfileEntity userProfile;
    @ManyToOne
    @JoinColumn(name = "disease_id")
    private DiseaseEntity disease;
    @CreatedDate
    private LocalDateTime user_past_disease_created_at;
    @LastModifiedDate
    private LocalDateTime user_past_disease_updated_at;





}
