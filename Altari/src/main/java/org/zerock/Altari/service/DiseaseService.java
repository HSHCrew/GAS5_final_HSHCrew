package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.DiseaseDTO;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.repository.DiseaseRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "disease")
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    // 모든 질병 정보를 캐시
    @Transactional(readOnly = true)
    @Cacheable(key = "'allDiseases'")
    public List<DiseaseEntity> getAllDiseases() {
        List<DiseaseEntity> diseases = diseaseRepository.findAll();
        if (diseases.isEmpty()) {
            throw new EntityNotFoundException("No diseases found in the database");
        }
        return diseases;
    }

    // 특정 질병 정보를 캐시
    @Transactional(readOnly = true)
    @Cacheable(key = "#diseaseId")
    public DiseaseEntity getDiseaseInfo(Integer diseaseId) {
        DiseaseEntity disease = diseaseRepository.findByDiseaseId(diseaseId);
        if (disease == null) {
            throw new EntityNotFoundException("No disease found with the given ID");
        }
        return disease;
    }

    @Transactional
    @CacheEvict(key = "#diseaseId")
    public DiseaseEntity updateDisease(Integer diseaseId, DiseaseDTO updatedDisease) {
        DiseaseEntity disease = diseaseRepository.findByDiseaseId(diseaseId);
        if (disease == null) {
            throw new EntityNotFoundException("No disease found with the given ID");
        }

        disease.setDiseaseId(updatedDisease.getDiseaseId());
        disease.setDiseaseName(updatedDisease.getDiseaseName());
        disease.setDiseaseDefinition(updatedDisease.getDiseaseDefinition());
        disease.setCause(updatedDisease.getCause());
        disease.setAttention(updatedDisease.getAttention());
        disease.setClassification(updatedDisease.getClassification());
        disease.setEtcInfo(updatedDisease.getEtcInfo());
        disease.setIsHereditary(updatedDisease.getIsHereditary());
        disease.setTreatment(updatedDisease.getTreatment());
        disease.setLifeAttention(updatedDisease.getLifeAttention());
        // 다른 필드도 업데이트
        return diseaseRepository.save(disease);
    }

    @CacheEvict(allEntries = true)
    public void clearAllCache() {
        // 아무 작업도 하지 않음 - 캐시만 초기화
    }
}
