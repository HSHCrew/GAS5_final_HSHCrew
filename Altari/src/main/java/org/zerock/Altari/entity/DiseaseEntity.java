package org.zerock.Altari.entity;

import ch.qos.logback.classic.spi.LoggingEventVO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

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
    private Integer diseaseId;
    private String disease_code;
    private String disease_name;
    @CreatedDate
    private LocalDateTime disease_created_at;
    @LastModifiedDate
    private LocalDateTime disease_updated_at;
    private Boolean is_hereditary;

    public DiseaseEntity(int diseaseId) {
        this.diseaseId = diseaseId;
    }

}
