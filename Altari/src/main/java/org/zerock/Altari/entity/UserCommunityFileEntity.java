package org.zerock.Altari.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_community_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserCommunityFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_community_file_id")
    private Integer user_community_comment_id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_community_post_id")
    private UserCommunityPostEntity userCommunityPost;

    @Column(name = "user_community_file_name")  // 파일 이름
    private String userCommunityFileName;

    @Column(name = "user_community_file_path")  // 파일 경로
    private String userCommunityFilePath;

    @Column(name = "user_community_file_type")  // 파일 형식
    private String userCommunityFileType;


}
