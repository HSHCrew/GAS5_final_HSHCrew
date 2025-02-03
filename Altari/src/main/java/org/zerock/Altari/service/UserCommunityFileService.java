package org.zerock.Altari.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.Altari.entity.UserCommunityFileEntity;
import org.zerock.Altari.entity.UserCommunityPostEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.UserCommunityFileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserCommunityFileService {

    private final String uploadDir = "/uploads/";
    @Autowired
    private UserCommunityFileRepository userCommunityFileRepository;

    public List<String> uploadFiles(Integer postId, List<MultipartFile> files) {
        List<String> fileUrls = new ArrayList<String>();

        for (MultipartFile file : files) {
            try {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);

                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                String fileUrl = "/uploads/" + fileName;
                fileUrls.add(fileUrl);

                saveFileInfoToDB(postId, fileName, fileUrl);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return fileUrls;
    }

    // 파일 삭제 메서드
    public void deleteFile(Integer postId, Integer fileId) {
        // 1. DB에서 파일 정보 가져오기
        UserCommunityFileEntity fileEntity = userCommunityFileRepository.findById(fileId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // 2. 파일 경로 확인 후 파일 시스템에서 삭제
        Path filePath = Paths.get(uploadDir + fileEntity.getUserCommunityFileName());
        try {
            Files.deleteIfExists(filePath); // 실제 파일 삭제
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete the file: " + fileEntity.getUserCommunityFileName(), e);
        }

        // 3. DB에서 파일 정보 삭제
        userCommunityFileRepository.delete(fileEntity);
    }

    private void saveFileInfoToDB(Integer postId, String fileName, String fileUrl) {
        UserCommunityFileEntity fileEntity = UserCommunityFileEntity.builder()
                .userCommunityPost(UserCommunityPostEntity.builder().userCommunityPostId(postId).build())
                .userCommunityFileName(fileName)
                .userCommunityFilePath(fileUrl)
                .userCommunityFileType("image/png")  // 실제 타입을 추출하려면 file.getContentType() 사용
                .build();

        userCommunityFileRepository.save(fileEntity);
    }
}
