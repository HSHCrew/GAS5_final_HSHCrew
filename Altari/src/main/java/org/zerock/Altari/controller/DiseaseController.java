package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.exception.EntityNotFoundException;
import org.zerock.Altari.repository.DiseaseRepository;

import java.util.List;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseRepository diseaseRepository;

    @GetMapping("/disease/list")
    public List<DiseaseEntity> getAllDiseases() {
        List<DiseaseEntity> diseases = diseaseRepository.findAll();
        if (diseases.isEmpty()) {
            throw new EntityNotFoundException("No diseases found in the database");
        }
        return diseases;
    }
}