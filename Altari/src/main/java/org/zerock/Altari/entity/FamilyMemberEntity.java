package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_member")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FamilyMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_member_id")
    private Integer familyMemberId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "family_group_id")
    private FamilyGroupEntity familyGroup;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "on_group_management")
    private Boolean onGroupManagement;

    @Column(name = "family_member_created_at")
    @CreatedDate
    private LocalDateTime familyMemberCreatedAt;

    @Column(name = "family_member_updated_at")
    @LastModifiedDate
    private LocalDateTime familyMemberUpdatedAt;


}
