package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.ArticleDTO;
import org.zerock.Altari.dto.NewsCurationDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.repository.*;

import java.util.*;

@Service
@CacheConfig(cacheNames = "newsCuration")
@Log4j2
@RequiredArgsConstructor
public class NewsCurationService {

    private final UserRepository userRepository;
    private final DiseaseRepository diseaseRepository;
    private final MedicationRepository medicationRepository;
    private final NewsCurationRepository newsCurationRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public List<NewsCurationDTO> getNewsCurationByUserId(UserEntity user) {

        Set<DiseaseEntity> diseaseEntities = new HashSet<>();

        // user의 질병 정보 가져오기 (userDiseases, userPastDiseases, familyHistories)
        diseaseEntities.addAll(user.getUserDiseases()); // 현재 질병
        diseaseEntities.addAll(user.getUserPastDiseases()); // 과거 질병
        diseaseEntities.addAll(user.getFamilyHistories()); // 가족력

        // DiseaseEntity와 관련된 NewsCurationEntity 가져오기
        List<NewsCurationEntity> newsCurationEntities = new ArrayList<>();
        for (DiseaseEntity disease : diseaseEntities) {
            NewsCurationEntity newsCuration = newsCurationRepository.findByDisease(disease);
            if (newsCuration != null) {
                newsCurationEntities.add(newsCuration);
            }
        }

        // NewsCurationEntity -> NewsCurationDTO 변환
        List<NewsCurationDTO> result = new ArrayList<>();
        for (NewsCurationEntity newsCuration : newsCurationEntities) {
            DiseaseEntity disease = newsCuration.getDisease();

            // Articles 변환
            List<ArticleEntity> articleEntities = articleRepository.findByDisease(disease);
            List<ArticleDTO> articleDTOs = new ArrayList<>();
            for (ArticleEntity article : articleEntities) {
                ArticleDTO articleDTO = ArticleDTO.builder()
                        .topic(article.getTopic())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .date(article.getDate() != null ? article.getDate().toString() : "")  // LocalDateTime을 String으로 변환
                        .link(article.getLink())
                        .imageUrl(article.getImageUrl())
                        .diseaseName(disease.getDiseaseName()) // DiseaseName 추가
                        .build();
                articleDTOs.add(articleDTO);
            }

            // NewsCurationDTO로 변환
            NewsCurationDTO newsCurationDTO = NewsCurationDTO.builder()
                    .newsCurationId(newsCuration.getNewsCurationId())
                    .keyword(newsCuration.getKeyword())
                    .curationContent(newsCuration.getCurationContent())
                    .koreanCurationContent(newsCuration.getKoreanCurationContent())
                    .generatedAt(newsCuration.getGeneratedAt() != null ? newsCuration.getGeneratedAt().toString() : "") // null일 경우 빈 문자열
                    .disease(newsCuration.getDisease().getDiseaseName())  // DiseaseEntity 이름만 포함
                    .articles(articleDTOs)  // 변환된 ArticleDTO 리스트 포함
                    .build();

            result.add(newsCurationDTO);
        }

        return result;
    }

}