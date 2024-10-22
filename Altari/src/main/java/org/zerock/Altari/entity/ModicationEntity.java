package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "modication")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ModicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int modication_id;
    private String modication_name;
    private String modication_info;
    private String interaction_info;
}

