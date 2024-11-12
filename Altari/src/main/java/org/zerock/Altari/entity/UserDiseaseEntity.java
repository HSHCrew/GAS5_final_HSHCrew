package org.zerock.Altari.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_disease")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserDiseaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_disease_id")
    private Integer userDiseaseId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_profile_id")
    @JsonBackReference
    private UserProfileEntity userProfile;

    @ManyToOne
    @JoinColumn(name = "disease_id")
    private DiseaseEntity disease;

    @CreatedDate
    private LocalDateTime user_disease_created_at;

    @LastModifiedDate
    private LocalDateTime user_disease_updated_at;







}
