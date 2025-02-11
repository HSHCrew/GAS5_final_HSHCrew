package org.zerock.Altari.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "family_group")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FamilyGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_group_id")
    private Integer familyGroupId;

    @Column(name = "family_group_name", columnDefinition = "TEXT")
    private String familyGroupName;

    @Column(name = "family_group_created_at")
    @CreatedDate
    private LocalDateTime familyGroupCreatedAt;

    @Column(name = "family_group_updated_at")
    @LastModifiedDate
    private LocalDateTime familyGroupUpdatedAt;

    @OneToMany(mappedBy = "familyGroup", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<FamilyMemberEntity> familyMemberEntities = new ArrayList<>();

}
