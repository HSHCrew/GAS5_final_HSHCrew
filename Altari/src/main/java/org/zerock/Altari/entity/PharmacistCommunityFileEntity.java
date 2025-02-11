package org.zerock.Altari.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacist_community_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PharmacistCommunityFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pharmacist_community_file_id")
    private Integer pharmacistCommunityFileId;

    @ManyToOne
    @JoinColumn(name = "pharmacist_community_post_id")
    private PharmacistCommunityPostEntity pharmacistCommunityPost;

    @Column(name = "pharmacist_community_file_name") // 파일 이름
    private String pharmacistCommunityFileName;

    @Column(name = "pharmacist_community_file_path") // 파일 경로
    private String pharmacistCommunityFilePath;

    @Column(name = "pharmacist_community_file_type") // 파일 형식
    private String pharmacistCommunityFileType;
}

