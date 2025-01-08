package org.zerock.Altari.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_community_post_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCommunityPostCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_community_post_category_id")
    private Integer userCommunityPostCategoryId;

    @Column(name = "user_community_post_category_name", columnDefinition = "TEXT")
    private String userCommunityPostCategoryName;

}
