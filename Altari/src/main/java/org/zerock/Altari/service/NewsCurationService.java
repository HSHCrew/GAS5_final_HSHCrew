package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.NewsCurationDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.util.*;

@Service
@CacheConfig(cacheNames = "newsCuration")
@Log4j2
@RequiredArgsConstructor
public class NewsCurationService {

    @Autowired
    private UserDiseaseRepository userDiseaseRepository;
    @Autowired
    private UserPastDiseaseRepository userPastDiseaseRepository;
    @Autowired
    private FamilyHistoryRepository familyHistoryRepository;
    @Autowired
    private NewsCurationRepository newsCurationRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    @Cacheable(key = "#user")
    public List<NewsCurationDTO> getNewsCurationByUserId(UserEntity user) {

        Set<DiseaseEntity> diseaseEntities = new HashSet<>();

        Optional<List<UserDiseaseEntity>> optionalUserDiseases = userDiseaseRepository.findByUser(user);
        List<UserDiseaseEntity> userDiseases = optionalUserDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);

        for (UserDiseaseEntity userDisease : userDiseases) {
            DiseaseEntity disease = userDisease.getDisease(); // DiseaseEntity 가져오기
            if (disease != null) { // disease가 null인지 확인
                diseaseEntities.add(disease); // 중복 없이 추가
            }
        }

        Optional<List<UserPastDiseaseEntity>> optionalUserPastDiseases = userPastDiseaseRepository.findByUser(user);
        List<UserPastDiseaseEntity> userPastDiseases = optionalUserPastDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);

        // user_past_disease 테이블
        for (UserPastDiseaseEntity userPastDisease : userPastDiseases) {
            DiseaseEntity disease = userPastDisease.getDisease(); // DiseaseEntity 가져오기
            if (disease != null) { // disease가 null인지 확인
                diseaseEntities.add(disease); // 중복 없이 추가
            }
        }

        // family_history 테이블

        Optional<List<FamilyHistoryEntity>> optionalUserFamilyDiseases = familyHistoryRepository.findByUser(user);
        List<FamilyHistoryEntity> userFamilyDiseases = optionalUserFamilyDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);

        for (FamilyHistoryEntity familyHistory : userFamilyDiseases) {
            DiseaseEntity disease = familyHistory.getDisease(); // DiseaseEntity 가져오기
            if (disease != null) { // disease가 null인지 확인
                diseaseEntities.add(disease); // 중복 없이 추가
            }
        }

        // DiseaseEntity와 관련된 NewsCurationEntity 가져오기
        List<NewsCurationEntity> newsCurationEntities = new ArrayList<>();
        for (DiseaseEntity disease : diseaseEntities) {
            NewsCurationEntity newsCuration = newsCurationRepository.findByDisease(disease);
            if (newsCuration != null) { // NewsCurationEntity가 존재하면 추가
                newsCurationEntities.add(newsCuration);
            }
        }

        // 결과를 DTO 형태로 반환하기 위한 리스트
        List<NewsCurationDTO> result = new ArrayList<>();

        // NewsCurationEntity 리스트를 순회하면서 DTO 변환
        for (NewsCurationEntity newsCuration : newsCurationEntities) {
            // DiseaseEntity를 기반으로 연관된 ArticleEntity 리스트 가져오기
            DiseaseEntity disease = newsCuration.getDisease();
            List<ArticleEntity> articles = articleRepository.findByDisease(disease);

            // NewsCurationDTO로 변환
            NewsCurationDTO newsCurationDTO = NewsCurationDTO.builder()
                    .newsCurationId(newsCuration.getNewsCurationId())
                    .keyword(newsCuration.getKeyword())
                    .curationContent(newsCuration.getCurationContent())
                    .koreanCurationContent(newsCuration.getKoreanCurationContent())
                    .generatedAt(newsCuration.getGeneratedAt().toString())
                    .disease(newsCuration.getDisease())  // DiseaseEntity 포함
                    .article(newsCuration.getArticle())  // 한 개의 MedicationEntity 포함
                    .articles(articles)  // 여러 개의 ArticleEntity 포함
                    .build();

            // DTO를 결과에 추가
            result.add(newsCurationDTO);
        }

        return result;
    }


}
