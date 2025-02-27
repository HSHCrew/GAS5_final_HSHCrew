package org.zerock.Altari.entity;

import lombok.*;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medication_completion")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MedicationCompletionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer medication_completion_id; // Primary Key

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @CreatedDate
    private LocalDate createdAt; // 생성일자

    @Column(nullable = false)
    private Boolean morningTaken; // 아침 복약 여부

    @Column(nullable = false)
    private Boolean lunchTaken; // 점심 복약 여부

    @Column(nullable = false)
    private Boolean dinnerTaken; // 저녁 복약 여부

    @Column(nullable = false)
    private Boolean nightTaken; // 밤 복약 여부

}