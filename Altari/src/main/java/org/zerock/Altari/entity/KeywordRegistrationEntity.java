package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "keyword_registration")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class KeywordRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer keyword_registration_id;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfileEntity user_profile_id;
    @ManyToOne
    @JoinColumn(name = "health_keyword_id")
    private HealthKeywordEntity health_keyword_id;
    @CreatedDate
    private LocalDateTime keyword_registration_created_at;
    @LastModifiedDate
    private LocalDateTime keyword_registration_updated_at;
}
