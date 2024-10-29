package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.exception.EntityNotFoundException;
import org.zerock.Altari.repository.MedicationRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Log4j2
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationRepository medicationRepository;

    @GetMapping("/drugs")
    public List<MedicationEntity> getAllDrugs() {
        List<MedicationEntity> drugs = medicationRepository.findAll();
        if (drugs.isEmpty()) {
            throw new EntityNotFoundException("No medications found in the database");
        }
        return drugs;
    }


}
