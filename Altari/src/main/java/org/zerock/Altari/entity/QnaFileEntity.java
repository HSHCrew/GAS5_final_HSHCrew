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
@Table(name = "qna_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_file_id")
    private Integer qnaFileId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "qna_post_id")
    private QnaPostEntity qnaPost;

    @Column(name = "qna_file_name") // 파일 이름
    private String qnaFileName;

    @Column(name = "qna_file_path") // 파일 경로
    private String qnaFilePath;

    @Column(name = "qna_file_type") // 파일 형식
    private String qnaFileType;
}

