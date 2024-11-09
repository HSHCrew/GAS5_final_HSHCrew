package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    private String username;

    private String password;

    private String role;
    @CreatedDate
    private LocalDateTime user_created_at;
    @LastModifiedDate
    private LocalDateTime user_updated_at;

    public void changePassword(String password) {
        this.password = password;
    }

    public UserEntity(String username) {
        this.username = username;
        this.password = password;
        this.role = role;
    }



}




