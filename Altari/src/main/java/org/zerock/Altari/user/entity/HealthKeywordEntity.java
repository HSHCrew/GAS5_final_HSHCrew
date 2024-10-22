package org.zerock.Altari.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_keyword")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class HealthKeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int health_keyword_id;
    private String health_keyword;
    @CreatedDate
    private LocalDateTime create_at;
    @LastModifiedDate
    private LocalDateTime update_at;
}