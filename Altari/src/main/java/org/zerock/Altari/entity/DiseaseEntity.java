package org.zerock.Altari.entity;

import ch.qos.logback.classic.spi.LoggingEventVO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(name = "disease_code")
    private String diseaseCode;

    @Column(name = "disease_name")
    private String diseaseName;

    @Column(name = "disease_info")
    private String diseaseInfo;

    @CreatedDate
    private LocalDateTime disease_created_at;

    @LastModifiedDate
    private LocalDateTime disease_updated_at;

    @Column(name = "is_hereditary")
    private Boolean isHereditary;



    public DiseaseEntity(int diseaseId) {
        this.diseaseId = diseaseId;
    }

}
