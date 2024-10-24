package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MedicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int medication_id;
    private String medication_name;
    private String medication_info;
    private String interaction_info;
    @CreatedDate
    private LocalDateTime medication_created_at;
    @LastModifiedDate
    private LocalDateTime medication_updated_at;

    public MedicationEntity(int id) {
        this.medication_id = id;}
}

