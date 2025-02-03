package org.zerock.Altari.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.Altari.service.UserCommunityFileService;

import java.util.List;

@RestController
@RequestMapping("/altari/userCommunity/files")
public class UserCommunityFileController {

    @Autowired
    private UserCommunityFileService userCommunityFileService;

    // 파일 업로드 API
    @PostMapping("/{postId}/upload")
    public ResponseEntity<List<String>> uploadFiles(
            @PathVariable Integer postId,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            // 파일 업로드 수행
            List<String> fileUrls = userCommunityFileService.uploadFiles(postId, files);

            // 성공적인 파일 업로드 후, 파일 경로 리스트 반환
            return ResponseEntity.ok(fileUrls);
        } catch (Exception e) {
            // 실패 시 에러 메시지 반환
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{postId}/files/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Integer postId, @PathVariable Integer fileId) {

        try {
            userCommunityFileService.deleteFile(postId, fileId);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred while deleting the file: " + e.getMessage());
        }
    }
}
