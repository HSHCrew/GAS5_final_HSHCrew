package org.zerock.Altari.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacist_community_post_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacistCommunityPostCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pharmacist_community_post_category_id")
    private Integer pharmacistCommunityPostCategoryId;

    @Column(name = "pharmacist_community_post_category_name", columnDefinition = "TEXT")
    private String pharmacistCommunityPostCategoryName;
}

