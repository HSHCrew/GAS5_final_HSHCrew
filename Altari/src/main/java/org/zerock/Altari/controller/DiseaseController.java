package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.service.DiseaseService;

import java.util.List;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseService diseaseService;

    @GetMapping("/disease/list")
    public List<DiseaseEntity> getAllDiseases() {
        return diseaseService.getAllDiseases(); // 서비스에서 데이터를 가져옵니다.
    }

    @GetMapping("/disease-info/{diseaseId}")
    public DiseaseEntity getDiseaseInfo(@PathVariable Integer diseaseId) {
        return diseaseService.getDiseaseInfo(diseaseId); // 서비스에서 데이터를 가져옵니다.
    }
    @DeleteMapping("/disease/clear-cache")
    public ResponseEntity<String> clearCache() {
        diseaseService.clearAllCache();  // 캐시 초기화 메서드 호출
        return ResponseEntity.ok("All disease caches have been cleared.");
    }

}