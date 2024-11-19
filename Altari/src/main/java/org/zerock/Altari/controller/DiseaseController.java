package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.exception.EntityNotFoundException;
import org.zerock.Altari.repository.DiseaseRepository;
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

}