package org.zerock.Altari.dto;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.UserProfileEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FileDTO {

    private String fileName;  // 파일 이름
    private String filePath;  // 파일 경로(URL)
    private String fileType;  // 파일 타입 (예: image/png, application/pdf)
}
