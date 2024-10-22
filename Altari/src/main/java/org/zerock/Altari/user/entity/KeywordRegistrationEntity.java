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
public class KeywordRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int keyword_registration_id;
    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserEntity user_profile_id;
    @ManyToOne
    @JoinColumn(name = "health_keyword_id")
    private HealthKeywordEntity health_keyword_id;
    @CreatedDate
    private LocalDateTime create_at;
    @LastModifiedDate
    private LocalDateTime update_at;
}
