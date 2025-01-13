package org.zerock.Altari.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna_post_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaPostCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_post_category_id")
    private Integer qnaPostCategoryId;

    @Column(name = "qna_post_category_name", columnDefinition = "TEXT")
    private String qnaPostCategoryName;

}
