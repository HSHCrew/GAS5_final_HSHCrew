package org.zerock.Altari.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.repository.MedicationRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;

    @Autowired
    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "medications", key = "'all_drugs'") // 모든 약을 캐시 처리
    public List<MedicationEntity> getAllDrugs() {
        List<MedicationEntity> drugs = medicationRepository.findAll();
        if (drugs.isEmpty()) {
            throw new EntityNotFoundException("No medications found in the database");
        }
        return drugs;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "medications", key = "#medicationId") // 특정 약 정보를 캐시 처리
    public MedicationEntity getDrugInfo(Integer medicationId) {
        MedicationEntity medication = medicationRepository.findByMedicationId(medicationId);
        if (medication == null) {
            throw new EntityNotFoundException("No medication found for the given ID");
        }
        return medication;
    }
}