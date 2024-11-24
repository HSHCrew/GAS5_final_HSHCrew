package org.zerock.Altari.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zerock.Altari.dto.NewsCurationDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.repository.*;

import java.util.*;

@Service
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

    public List<NewsCurationDTO> getNewsCurationByUserId(UserEntity user) {

        UserProfileEntity userProfile = userProfileRepository.findByUsername(user);

        Set<DiseaseEntity> diseaseEntities = new HashSet<>();

        List<UserDiseaseEntity> userDiseases = userDiseaseRepository.findByUserProfile(userProfile);
        for (UserDiseaseEntity userDisease : userDiseases) {
            DiseaseEntity disease = userDisease.getDisease(); // DiseaseEntity 가져오기
            if (disease != null) { // disease가 null인지 확인
                diseaseEntities.add(disease); // 중복 없이 추가
            }
        }

        // user_past_disease 테이블
        List<UserPastDiseaseEntity> userPastDiseases = userPastDiseaseRepository.findByUserProfile(userProfile);
        for (UserPastDiseaseEntity userPastDisease : userPastDiseases) {
            DiseaseEntity disease = userPastDisease.getDisease(); // DiseaseEntity 가져오기
            if (disease != null) { // disease가 null인지 확인
                diseaseEntities.add(disease); // 중복 없이 추가
            }
        }

        // family_history 테이블
        List<FamilyHistoryEntity> familyHistories = familyHistoryRepository.findByUserProfile(userProfile);
        for (FamilyHistoryEntity familyHistory : familyHistories) {
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
