package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Cacheable(value = "userHealthInfos", key = "#username")
    public UserHealthInfoDTO getUserHealthInfo(UserEntity username) {

        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }

        UserProfileEntity userProfile = optionalUserProfile.get();

        // 사용자 프로필에 따라 관련된 모든 엔티티를 가져옴
        List<UserDiseaseEntity> userDiseases = userDiseaseRepository.findByUserProfile(userProfile);
        List<UserPastDiseaseEntity> userPastDiseases = userPastDiseaseRepository.findByUserProfile(userProfile);
        List<FamilyHistoryEntity> userFamilyDiseases = familyHistoryRepository.findByUserProfile(userProfile);
        List<AllergyEntity> userAllergies = allergyRepository.findByUserProfile(userProfile);

        // 각 엔티티 리스트를 DTO에 설정
        UserHealthInfoDTO userHealthInfoDTO = new UserHealthInfoDTO();
        userHealthInfoDTO.setDiseases(userDiseases.stream()
                .map(UserDiseaseEntity::getDisease)
                .collect(Collectors.toList()));

        userHealthInfoDTO.setPastDiseases(userPastDiseases.stream()
                .map(pastDisease -> pastDisease.getDisease())
                .collect(Collectors.toList()));

        userHealthInfoDTO.setFamilyDiseases(userFamilyDiseases.stream()
                .map(FamilyHistoryEntity::getDisease)
                .collect(Collectors.toList()));

        userHealthInfoDTO.setAllergyMedications(userAllergies.stream()
                .map(AllergyEntity::getMedication) // Medication 전체를 반환
                .collect(Collectors.toList()));

        return userHealthInfoDTO;
    }



    @Transactional
    public String updateUserDisease(UserEntity username,
                                       UserHealthInfoDTO userHealthInfoDTO) {


        // 사용자 프로필 조회
        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }


        try {

            UserProfileEntity userProfileEntity = userProfileRepository.findByUsername(username);

            List<DiseaseEntity> inputDiseaseIds = userHealthInfoDTO.getDiseases();

            List<UserDiseaseEntity> userDiseases = userDiseaseRepository.findByUserProfile(userProfileEntity);

// 현재 질병 테이블에서 제거해야 할 질병 ID 목록
            List<UserDiseaseEntity> diseasesToRemove = new ArrayList<>();

            for (UserDiseaseEntity currentDisease : userDiseases) {
                if (!inputDiseaseIds.contains(currentDisease.getDisease().getDiseaseId())) {
                    diseasesToRemove.add(currentDisease);
                }
            }
            for (UserDiseaseEntity diseaseToRemove : diseasesToRemove) {
                diseaseToRemove.setUserProfile(null); // `user_profile_id`를 null로 설정
            }
            userDiseaseRepository.deleteAll(diseasesToRemove); // `user_profile`과의 관계를 끊은 후 삭제


// 새로운 질병 ID 추가
            List<UserDiseaseEntity> newUserDiseases = new ArrayList<>();


            for (DiseaseEntity diseaseId : inputDiseaseIds) {
                if (userDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(diseaseId))) {
                    UserDiseaseEntity userDisease = UserDiseaseEntity.builder()
                            .userProfile(userProfileEntity)
                            .disease(new DiseaseEntity(diseaseId.getDiseaseId()))
                            .build();
                    newUserDiseases.add(userDisease);
                }
            }

            userDiseaseRepository.saveAll(newUserDiseases);



            return "UserDisease info updated successfully";


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public String updateUserPastDisease(UserEntity username,
                                       UserHealthInfoDTO userHealthInfoDTO) {


        // 사용자 프로필 조회
        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }


        try {

            UserProfileEntity userProfileEntity = userProfileRepository.findByUsername(username);

            List<DiseaseEntity> inputPastDiseaseIds = userHealthInfoDTO.getPastDiseases();

            List<UserPastDiseaseEntity> userPastDiseases = userPastDiseaseRepository.findByUserProfile(userProfileEntity);

// 현재 질병 테이블에서 제거해야 할 질병 ID 목록

            List<UserPastDiseaseEntity> pastDiseasesToRemove = new ArrayList<>();

            for (UserPastDiseaseEntity currentPastDisease : userPastDiseases) {
                if (!inputPastDiseaseIds.contains(currentPastDisease.getDisease().getDiseaseId())) {
                    pastDiseasesToRemove.add(currentPastDisease);
                }
            }
            for (UserPastDiseaseEntity diseaseToRemove : pastDiseasesToRemove) {
                diseaseToRemove.setUserProfile(null); // `user_profile_id`를 null로 설정
            }
            userPastDiseaseRepository.deleteAll(pastDiseasesToRemove);


// 새로운 질병 ID 추가

            List<UserPastDiseaseEntity> newUserPastDiseases = new ArrayList<>();



            for (DiseaseEntity pastDiseaseId : inputPastDiseaseIds) {
                if (userPastDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(pastDiseaseId))) {
                    UserPastDiseaseEntity userPastDisease = UserPastDiseaseEntity.builder()
                            .userProfile(userProfileEntity)
                            .disease(new DiseaseEntity(pastDiseaseId.getDiseaseId()))
                            .build();
                    newUserPastDiseases.add(userPastDisease);
                }
            }

            userPastDiseaseRepository.saveAll(newUserPastDiseases);



            return "UserPastDisease info updated successfully";


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public String updateUserFamilyDisease(UserEntity username,
                                       UserHealthInfoDTO userHealthInfoDTO) {


        // 사용자 프로필 조회
        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }


        try {

            UserProfileEntity userProfileEntity = userProfileRepository.findByUsername(username);
            List<DiseaseEntity> inputFamilyDiseaseIds = userHealthInfoDTO.getFamilyDiseases();

            List<FamilyHistoryEntity> userFamilyDiseases = familyHistoryRepository.findByUserProfile(userProfileEntity);

// 현재 질병 테이블에서 제거해야 할 질병 ID 목록
            List<FamilyHistoryEntity> familyDiseasesToRemove = new ArrayList<>();

            for (FamilyHistoryEntity currentFamilyDisease : userFamilyDiseases) {
                if (!inputFamilyDiseaseIds.contains(currentFamilyDisease.getDisease().getDiseaseId())) {
                    familyDiseasesToRemove.add(currentFamilyDisease);
                }
            }
            for (FamilyHistoryEntity diseaseToRemove : familyDiseasesToRemove) {
                diseaseToRemove.setUserProfile(null); // `user_profile_id`를 null로 설정
            }
            familyHistoryRepository.deleteAll(familyDiseasesToRemove);



// 새로운 질병 ID 추가
            List<FamilyHistoryEntity> newFamilyDiseases = new ArrayList<>();



            for (DiseaseEntity familyDiseaseId : inputFamilyDiseaseIds) {
                if (userFamilyDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(familyDiseaseId))) {
                    FamilyHistoryEntity familyDisease = FamilyHistoryEntity.builder()
                            .userProfile(userProfileEntity)
                            .disease(new DiseaseEntity(familyDiseaseId.getDiseaseId()))
                            .build();
                    newFamilyDiseases.add(familyDisease);
                }
            }

            familyHistoryRepository.saveAll(newFamilyDiseases);

            return "UserFamilyDisease info updated successfully";


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public String updateUserAllergy(UserEntity username, UserHealthInfoDTO userHealthInfoDTO) {
        try {
            // 사용자 프로필 조회
            UserProfileEntity userProfileEntity = userProfileRepository.findByUsername(username);
            if (userProfileEntity == null) {
                throw new RuntimeException("User profile not found for username: " + username);
            }

            List<MedicationEntity> inputAllergyMedicationsIds = userHealthInfoDTO.getAllergyMedications();

            // 현재 사용자의 알레르기 목록 조회
            List<AllergyEntity> userAllergyMedications = allergyRepository.findByUserProfile(userProfileEntity);

            // 현재 질병 테이블에서 제거해야 할 질병 ID 목록
            List<AllergyEntity> allergyMedicationsToRemove = new ArrayList<>();

            for (AllergyEntity currentAllergyMedication : userAllergyMedications) {
                MedicationEntity currentMedication = currentAllergyMedication.getMedication();
                if (currentMedication != null && !inputAllergyMedicationsIds.contains(currentMedication)) {
                    allergyMedicationsToRemove.add(currentAllergyMedication);
                }
            }

            for (AllergyEntity medicationToRemove : allergyMedicationsToRemove) {
                medicationToRemove.setUserProfile(null); // `user_profile_id`를 null로 설정
            }
            allergyRepository.deleteAll(allergyMedicationsToRemove);

            // 알레르기 목록에서 제거할 항목을 삭제
            if (!allergyMedicationsToRemove.isEmpty()) {
                allergyRepository.deleteAll(allergyMedicationsToRemove);
            }


            // 새로운 질병 ID 추가
            List<AllergyEntity> newAllergyMedications = new ArrayList<>();

            for (MedicationEntity inputMedication : inputAllergyMedicationsIds) {
                if (inputMedication != null && userAllergyMedications.stream()
                        .noneMatch(existingAllergy -> existingAllergy.getMedication() != null
                                && existingAllergy.getMedication().getMedicationId().equals(inputMedication.getMedicationId()))) {
                    AllergyEntity newAllergyMedication = AllergyEntity.builder()
                            .userProfile(userProfileEntity)
                            .medication(inputMedication)
                            .build();
                    newAllergyMedications.add(newAllergyMedication);
                }
            }

            // 새로운 알레르기 데이터를 저장
            if (!newAllergyMedications.isEmpty()) {
                allergyRepository.saveAll(newAllergyMedications);
            }

            return "User allergy information updated successfully";

        } catch (Exception e) {
            throw new RuntimeException("Error occurred while updating user allergy information: " + e.getMessage(), e);
        }
    }




}
