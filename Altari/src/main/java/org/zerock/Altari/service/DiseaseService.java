package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.repository.DiseaseRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    // 모든 질병 정보를 캐시
    @Transactional(readOnly = true)
    @Cacheable(value = "diseases", key = "'all_diseases'")
    public List<DiseaseEntity> getAllDiseases() {
        List<DiseaseEntity> diseases = diseaseRepository.findAll();
        if (diseases.isEmpty()) {
            throw new EntityNotFoundException("No diseases found in the database");
        }
        return diseases;
    }

    // 특정 질병 정보를 캐시
    @Cacheable(value = "diseases", key = "#diseaseId")
    @Transactional(readOnly = true)
    public DiseaseEntity getDiseaseInfo(Integer diseaseId) {
        DiseaseEntity disease = diseaseRepository.findByDiseaseId(diseaseId);
        if (disease == null) {
            throw new EntityNotFoundException("No disease found with the given ID");
        }
        return disease;
    }
}
