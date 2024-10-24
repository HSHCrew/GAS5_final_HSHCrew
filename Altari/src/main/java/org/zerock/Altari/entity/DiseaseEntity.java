package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "disease")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DiseaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int disease_id;
    private String disease_name;
    private String disease_info;
    @CreatedDate
    private LocalDateTime disease_created_at;
    @LastModifiedDate
    private LocalDateTime disease_updated_at;
    private boolean is_hereditary;

    public DiseaseEntity(int id) {
        this.disease_id = id;
    }
}
