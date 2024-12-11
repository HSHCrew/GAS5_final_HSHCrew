package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserHealthInfoDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.util.ArrayList;
import java.util.List;
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
    @Autowired
    private MedicationRepository medicationRepository;

    @Transactional(readOnly = true)
    public UserHealthInfoDTO getUserHealthInfo(UserEntity user) {

        // 사용자 프로필에 따라 관련된 모든 엔티티를 가져옴
        Optional<List<UserDiseaseEntity>> optionalUserDiseases = userDiseaseRepository.findByUser(user);
        Optional<List<UserPastDiseaseEntity>> optionalUserPastDiseases = userPastDiseaseRepository.findByUser(user);
        Optional<List<FamilyHistoryEntity>> optionalUserFamilyDiseases = familyHistoryRepository.findByUser(user);
        Optional<List<AllergyEntity>> optionalUserAllergies = allergyRepository.findByUser(user);

        List<UserDiseaseEntity> userDiseases = optionalUserDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);
        List<UserPastDiseaseEntity> userPastDiseases = optionalUserPastDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);
        List<FamilyHistoryEntity> userFamilyDiseases = optionalUserFamilyDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);
        List<AllergyEntity> userAllergies = optionalUserAllergies.orElseThrow(UserExceptions.NOT_FOUND::get);

        // 각 엔티티 리스트를 DTO에 설정
        UserHealthInfoDTO userHealthInfoDTO = new UserHealthInfoDTO();
        userHealthInfoDTO.setDiseases(userDiseases.stream().map(UserDiseaseEntity::getDisease).collect(Collectors.toList()));

        userHealthInfoDTO.setPastDiseases(userPastDiseases.stream().map(pastDisease -> pastDisease.getDisease()).collect(Collectors.toList()));

        userHealthInfoDTO.setFamilyDiseases(userFamilyDiseases.stream().map(FamilyHistoryEntity::getDisease).collect(Collectors.toList()));

        userHealthInfoDTO.setAllergyMedications(userAllergies.stream().map(AllergyEntity::getMedication) // Medication 전체를 반환
                .collect(Collectors.toList()));

        return userHealthInfoDTO;
    }


    @Transactional
    public String updateUserDisease(UserEntity user, UserHealthInfoDTO userHealthInfoDTO) {

        List<DiseaseEntity> inputDiseaseIds = userHealthInfoDTO.getDiseases();
        Optional<List<UserDiseaseEntity>> optionalUserDiseases = userDiseaseRepository.findByUser(user);
        List<UserDiseaseEntity> userDiseases = optionalUserDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);

// 현재 질병 테이블에서 제거해야 할 질병 ID 목록
        List<UserDiseaseEntity> diseasesToRemove = new ArrayList<>();

        for (UserDiseaseEntity currentDisease : userDiseases) {
            if (!inputDiseaseIds.contains(currentDisease.getDisease().getDiseaseId())) {
                diseasesToRemove.add(currentDisease);
            }
        }
        for (UserDiseaseEntity diseaseToRemove : diseasesToRemove) {
            diseaseToRemove.setUser(null); // `user_profile_id`를 null로 설정
        }
        userDiseaseRepository.deleteAll(diseasesToRemove); // `user_profile`과의 관계를 끊은 후 삭제


// 새로운 질병 ID 추가
        List<UserDiseaseEntity> newUserDiseases = new ArrayList<>();


        for (DiseaseEntity diseaseId : inputDiseaseIds) {
            if (userDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(diseaseId))) {
                UserDiseaseEntity userDisease = UserDiseaseEntity.builder().user(user).disease(new DiseaseEntity(diseaseId.getDiseaseId())).build();
                newUserDiseases.add(userDisease);
            }
        }

        userDiseaseRepository.saveAll(newUserDiseases);


        return "UserDisease info updated successfully";

    }

    @Transactional
    public String updateUserPastDisease(UserEntity user, UserHealthInfoDTO userHealthInfoDTO) {


        Optional<List<UserPastDiseaseEntity>> optionalUserPastDiseases = userPastDiseaseRepository.findByUser(user);

        List<DiseaseEntity> inputPastDiseaseIds = userHealthInfoDTO.getPastDiseases();

        List<UserPastDiseaseEntity> userPastDiseases = optionalUserPastDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);
// 현재 질병 테이블에서 제거해야 할 질병 ID 목록

        List<UserPastDiseaseEntity> pastDiseasesToRemove = new ArrayList<>();

        for (UserPastDiseaseEntity currentPastDisease : userPastDiseases) {
            if (!inputPastDiseaseIds.contains(currentPastDisease.getDisease().getDiseaseId())) {
                pastDiseasesToRemove.add(currentPastDisease);
            }
        }
        for (UserPastDiseaseEntity diseaseToRemove : pastDiseasesToRemove) {
            diseaseToRemove.setUser(null); // `user_profile_id`를 null로 설정
        }
        userPastDiseaseRepository.deleteAll(pastDiseasesToRemove);


// 새로운 질병 ID 추가

        List<UserPastDiseaseEntity> newUserPastDiseases = new ArrayList<>();


        for (DiseaseEntity pastDiseaseId : inputPastDiseaseIds) {
            if (userPastDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(pastDiseaseId))) {
                UserPastDiseaseEntity userPastDisease = UserPastDiseaseEntity.builder().user(user).disease(new DiseaseEntity(pastDiseaseId.getDiseaseId())).build();
                newUserPastDiseases.add(userPastDisease);
            }
        }

        userPastDiseaseRepository.saveAll(newUserPastDiseases);


        return "UserPastDisease info updated successfully";


    }

    @Transactional
    public String updateUserFamilyDisease(UserEntity user, UserHealthInfoDTO userHealthInfoDTO) {


        List<DiseaseEntity> inputFamilyDiseaseIds = userHealthInfoDTO.getFamilyDiseases();
        Optional<List<FamilyHistoryEntity>> optionalUserFamilyDiseases = familyHistoryRepository.findByUser(user);
        List<FamilyHistoryEntity> userFamilyDiseases = optionalUserFamilyDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);


// 현재 질병 테이블에서 제거해야 할 질병 ID 목록
        List<FamilyHistoryEntity> familyDiseasesToRemove = new ArrayList<>();

        for (FamilyHistoryEntity currentFamilyDisease : userFamilyDiseases) {
            if (!inputFamilyDiseaseIds.contains(currentFamilyDisease.getDisease().getDiseaseId())) {
                familyDiseasesToRemove.add(currentFamilyDisease);
            }
        }
        for (FamilyHistoryEntity diseaseToRemove : familyDiseasesToRemove) {
            diseaseToRemove.setUser(null); // `user_profile_id`를 null로 설정
        }
        familyHistoryRepository.deleteAll(familyDiseasesToRemove);


// 새로운 질병 ID 추가
        List<FamilyHistoryEntity> newFamilyDiseases = new ArrayList<>();


        for (DiseaseEntity familyDiseaseId : inputFamilyDiseaseIds) {
            if (userFamilyDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(familyDiseaseId))) {
                FamilyHistoryEntity familyDisease = FamilyHistoryEntity.builder().user(user).disease(new DiseaseEntity(familyDiseaseId.getDiseaseId())).build();
                newFamilyDiseases.add(familyDisease);
            }
        }

        familyHistoryRepository.saveAll(newFamilyDiseases);

        return "UserFamilyDisease info updated successfully";


    }

    @Transactional
    public String updateUserAllergy(UserEntity user, UserHealthInfoDTO userHealthInfoDTO) {

        Optional<List<AllergyEntity>> optionalUserAllergies = allergyRepository.findByUser(user);

        List<AllergyEntity> userAllergyMedications = optionalUserAllergies.orElseThrow(UserExceptions.NOT_FOUND::get);


        List<MedicationEntity> inputAllergyMedicationsIds = userHealthInfoDTO.getAllergyMedications();

        List<AllergyEntity> allergyMedicationsToRemove = new ArrayList<>();

        for (AllergyEntity currentAllergyMedication : userAllergyMedications) {
            MedicationEntity currentMedication = currentAllergyMedication.getMedication();
            if (currentMedication != null && !inputAllergyMedicationsIds.contains(currentMedication)) {
                allergyMedicationsToRemove.add(currentAllergyMedication);
            }
        }

        for (AllergyEntity medicationToRemove : allergyMedicationsToRemove) {
            medicationToRemove.setUser(null); // `user_profile_id`를 null로 설정
        }
        allergyRepository.deleteAll(allergyMedicationsToRemove);

        // 알레르기 목록에서 제거할 항목을 삭제
        if (!allergyMedicationsToRemove.isEmpty()) {
            allergyRepository.deleteAll(allergyMedicationsToRemove);
        }


        // 새로운 질병 ID 추가
        List<AllergyEntity> newAllergyMedications = new ArrayList<>();

        for (MedicationEntity inputMedication : inputAllergyMedicationsIds) {
            if (inputMedication != null && userAllergyMedications.stream().noneMatch(existingAllergy -> existingAllergy.getMedication() != null && existingAllergy.getMedication().getMedicationId().equals(inputMedication.getMedicationId()))) {
                AllergyEntity newAllergyMedication = AllergyEntity.builder().user(user).medication(inputMedication).build();
                newAllergyMedications.add(newAllergyMedication);
            }
        }

        // 새로운 알레르기 데이터를 저장
        if (!newAllergyMedications.isEmpty()) {
            allergyRepository.saveAll(newAllergyMedications);
        }

        return "User allergy information updated successfully";

    }


}
