package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserDiseaseDTO;
import org.zerock.Altari.dto.UserHealthInfoDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserHealthInfoService {

    @Autowired
    private UserDiseaseRepository userDiseaseRepository;
    @Autowired
    private UserPastDiseaseRepository userPastDiseaseRepository;
    @Autowired
    private AllergyRepository allergyRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private FamilyHistoryRepository familyHistoryRepository;
    @Autowired
    private DiseaseRepository diseaseRepository;

    @Transactional(readOnly = true)
    public UserHealthInfoDTO getUserHealthInfo(UserEntity username) {

        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }

        UserProfileEntity userProfile = optionalUserProfile.get();

        List<UserDiseaseEntity> userDiseases = userDiseaseRepository.findByUserProfile(userProfile);
        List<UserPastDiseaseEntity> userPastDiseases = userPastDiseaseRepository.findByUserProfile(userProfile);
        List<FamilyHistoryEntity> userFamilyDiseases = familyHistoryRepository.findByUserProfile(userProfile);
        List<AllergyEntity> userAllergies = allergyRepository.findByUserProfile(userProfile);

        UserHealthInfoDTO userHealthInfoDTO = new UserHealthInfoDTO();

        userHealthInfoDTO.setDisease_id(userDiseases.stream()
                .map(disease -> disease.getDisease().getDiseaseId())
                .collect(Collectors.toList()));

        userHealthInfoDTO.setPast_disease_id(userPastDiseases.stream()
                .map(pastDisease -> pastDisease.getDisease().getDiseaseId())
                .collect(Collectors.toList()));

        userHealthInfoDTO.setFamily_disease_id(userFamilyDiseases.stream()
                .map(familyDisease -> familyDisease.getDisease().getDiseaseId())
                .collect(Collectors.toList()));

        userHealthInfoDTO.setAllergy_medication_id(userAllergies.stream()
                .map(disease -> disease.getMedication_id().getMedication_id())
                .collect(Collectors.toList()));

        return userHealthInfoDTO;
    }


    @Transactional
    public List<UserDiseaseEntity> updateUserHealthInfo(UserEntity username,
                                                        UserHealthInfoDTO userHealthInfoDTO) {


        // 사용자 프로필 조회
        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }


        try {

            UserProfileEntity userProfileEntity = userProfileRepository.findByUsername(username);

            List<Integer> inputDiseaseIds = userHealthInfoDTO.getDisease_id();
            List<Integer> inputPastDiseaseIds = userHealthInfoDTO.getPast_disease_id();
            List<Integer> inputFamilyDiseaseIds = userHealthInfoDTO.getFamily_disease_id();
            List<Integer> inputMedicationIds = userHealthInfoDTO.getAllergy_medication_id();

            List<UserDiseaseEntity> userDiseases = userDiseaseRepository.findByUserProfile(userProfileEntity);
            List<UserPastDiseaseEntity> userPastDiseases = userPastDiseaseRepository.findByUserProfile(userProfileEntity);
            List<FamilyHistoryEntity> userFamilyDiseases = familyHistoryRepository.findByUserProfile(userProfileEntity);
            List<AllergyEntity> userAllergies = allergyRepository.findByUserProfile(userProfileEntity);

// 현재 질병 테이블에서 제거해야 할 질병 ID 목록
            List<UserDiseaseEntity> diseasesToRemove = new ArrayList<>();
            List<UserPastDiseaseEntity> pastDiseasesToRemove = new ArrayList<>();
            List<FamilyHistoryEntity> familyDiseasesToRemove = new ArrayList<>();
            List<AllergyEntity> allergiesToRemove = new ArrayList<>();

            for (UserDiseaseEntity currentDisease : userDiseases) {
                if (!inputDiseaseIds.contains(currentDisease.getDisease().getDiseaseId())) {
                    diseasesToRemove.add(currentDisease);
                }
            }
            userDiseaseRepository.deleteAll(diseasesToRemove);

            for (UserPastDiseaseEntity currentPastDisease : userPastDiseases) {
                if (!inputPastDiseaseIds.contains(currentPastDisease.getDisease().getDiseaseId())) {
                    pastDiseasesToRemove.add(currentPastDisease);
                }
            }
            userPastDiseaseRepository.deleteAll(pastDiseasesToRemove);

            for (FamilyHistoryEntity currentFamilyDisease : userFamilyDiseases) {
                if (!inputFamilyDiseaseIds.contains(currentFamilyDisease.getDisease().getDiseaseId())) {
                    familyDiseasesToRemove.add(currentFamilyDisease);
                }
            }
            familyHistoryRepository.deleteAll(familyDiseasesToRemove);

            for (AllergyEntity currentAllergy : userAllergies) {
                if (!inputMedicationIds.contains(currentAllergy.getMedication_id().getMedication_id())) {
                    allergiesToRemove.add(currentAllergy);
                }
            }
            allergyRepository.deleteAll(allergiesToRemove);

// 새로운 질병 ID 추가
            List<UserDiseaseEntity> newUserDiseases = new ArrayList<>();
            List<UserPastDiseaseEntity> newUserPastDiseases = new ArrayList<>();
            List<FamilyHistoryEntity> newFamilyDiseases = new ArrayList<>();
            List<AllergyEntity> newAllergyMedications = new ArrayList<>();

            for (Integer diseaseId : inputDiseaseIds) {
                if (userDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(diseaseId))) {
                    UserDiseaseEntity userDisease = UserDiseaseEntity.builder()
                            .userProfile(userProfileEntity)
                            .disease(new DiseaseEntity(diseaseId))
                            .build();
                    newUserDiseases.add(userDisease);
                }
            }

            userDiseaseRepository.saveAll(newUserDiseases);

            for (Integer pastDiseaseId : inputPastDiseaseIds) {
                if (userPastDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(pastDiseaseId))) {
                    UserPastDiseaseEntity userPastDisease = UserPastDiseaseEntity.builder()
                            .userProfile(userProfileEntity)
                            .disease(new DiseaseEntity(pastDiseaseId))
                            .build();
                    newUserPastDiseases.add(userPastDisease);
                }
            }

            userPastDiseaseRepository.saveAll(newUserPastDiseases);

            for (Integer familyDiseaseId : inputFamilyDiseaseIds) {
                if (userFamilyDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(familyDiseaseId))) {
                    FamilyHistoryEntity familyDisease = FamilyHistoryEntity.builder()
                            .userProfile(userProfileEntity)
                            .disease(new DiseaseEntity(familyDiseaseId))
                            .build();
                    newFamilyDiseases.add(familyDisease);
                }
            }

            familyHistoryRepository.saveAll(newFamilyDiseases);

            for (Integer medicationId : inputMedicationIds) {
                if (userAllergies.stream().noneMatch(allergy -> allergy.getMedication_id().getMedication_id().equals(medicationId))) {
                    AllergyEntity allergy = AllergyEntity.builder()
                            .userProfile(userProfileEntity)
                            .medication_id(new MedicationEntity(medicationId))
                            .build();
                    newAllergyMedications.add(allergy);
                }
            }

            allergyRepository.saveAll(newAllergyMedications);

            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
