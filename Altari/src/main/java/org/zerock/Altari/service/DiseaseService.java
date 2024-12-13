package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.DiseaseDTO;
import org.zerock.Altari.entity.DiseaseEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.DiseaseRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

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
            throw CustomEntityExceptions.NOT_FOUND.get();
        }
        return diseases;
    }

    // 특정 질병 정보를 캐시
    @Transactional(readOnly = true)
    @Cacheable(key = "#diseaseId")
    public DiseaseEntity getDiseaseInfo(Integer diseaseId) {
        Optional<DiseaseEntity> optionalDisease = diseaseRepository.findByDiseaseId(diseaseId);
        DiseaseEntity disease = optionalDisease.orElseThrow(CustomEntityExceptions.NOT_FOUND::get);;

        return disease;
    }

    @Transactional
    @CacheEvict(key = "#diseaseId")
    public DiseaseEntity updateDisease(Integer diseaseId, DiseaseDTO updatedDisease) {
        Optional<DiseaseEntity> optionalDisease = diseaseRepository.findByDiseaseId(diseaseId);
        DiseaseEntity disease = optionalDisease.orElseThrow(CustomEntityExceptions.NOT_FOUND::get);;

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
