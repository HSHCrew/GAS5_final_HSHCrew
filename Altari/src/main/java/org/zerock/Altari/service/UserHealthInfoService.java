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
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserHealthInfoService {


    private final UserRepository userRepository;
    private final DiseaseRepository diseaseRepository;
    private final MedicationRepository medicationRepository;

    // 1. 현재 질병(user_disease) 추가
    public void addCurrentDisease(UserEntity user, Integer diseaseId) {
        DiseaseEntity disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        user.getUserDiseases().add(disease);
        userRepository.save(user);
    }

    // 2. 과거 질병(user_past_disease) 추가
    public void addPastDisease(UserEntity user, Integer diseaseId) {
        DiseaseEntity disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        user.getUserPastDiseases().add(disease);
        userRepository.save(user);
    }

    // 3. 가족력(family_history) 추가
    public void addFamilyHistory(UserEntity user, Integer diseaseId) {
        DiseaseEntity disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        user.getFamilyHistories().add(disease);
        userRepository.save(user);
    }

    // 4. 알레르기(allergy) 추가
    public void addAllergy(UserEntity user, Integer medicationId) {
        MedicationEntity medication = medicationRepository.findByMedicationId(medicationId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        user.getAllergies().add(medication);
        userRepository.save(user);
    }

    public UserHealthInfoDTO getUserHealthInfo(UserEntity user) {
        Set<DiseaseEntity> currentDiseases = Optional.ofNullable(user.getUserDiseases())
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        Set<DiseaseEntity> pastDiseases = Optional.ofNullable(user.getUserPastDiseases())
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        Set<DiseaseEntity> familyHistories = Optional.ofNullable(user.getFamilyHistories())
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        Set<MedicationEntity> allergies = Optional.ofNullable(user.getAllergies())
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        // DTO로 변환
        return new UserHealthInfoDTO(
                currentDiseases.stream().map(DiseaseEntity::getDiseaseName).collect(Collectors.toSet()),
                pastDiseases.stream().map(DiseaseEntity::getDiseaseName).collect(Collectors.toSet()),
                familyHistories.stream().map(DiseaseEntity::getDiseaseName).collect(Collectors.toSet()),
                allergies.stream().map(MedicationEntity::getMedicationName).collect(Collectors.toSet())
        );
    }

    // 9. 현재 질병(user_disease) 삭제
    public void removeCurrentDisease(UserEntity user, Integer diseaseId) {
        DiseaseEntity disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        user.getUserDiseases().remove(disease);
        userRepository.save(user);
    }

    // 10. 과거 질병(user_past_disease) 삭제
    public void removePastDisease(UserEntity user, Integer diseaseId) {
        DiseaseEntity disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        user.getUserPastDiseases().remove(disease);
        userRepository.save(user);
    }

    // 11. 가족력(family_history) 삭제
    public void removeFamilyHistory(UserEntity user, Integer diseaseId) {
        DiseaseEntity disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        user.getFamilyHistories().remove(disease);
        userRepository.save(user);
    }

    // 12. 알레르기(allergy) 삭제
    public void removeAllergy(UserEntity user, Integer medicationId) {
        MedicationEntity medication = medicationRepository.findByMedicationId(medicationId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        user.getAllergies().remove(medication);
        userRepository.save(user);
    }
}
//
//        // 사용자 프로필에 따라 관련된 모든 엔티티를 가져옴
//        List<UserDiseaseEntity> userDiseases = userDiseaseRepository.findByUser(user)
//                .orElseThrow(UserExceptions.NOT_FOUND::get);
//
//        List<UserPastDiseaseEntity> userPastDiseases = userPastDiseaseRepository.findByUser(user)
//                .orElseThrow(UserExceptions.NOT_FOUND::get);
//
//        List<FamilyHistoryEntity> userFamilyDiseases = familyHistoryRepository.findByUser(user)
//                .orElseThrow(UserExceptions.NOT_FOUND::get);
//
//        List<AllergyEntity> userAllergies = allergyRepository.findByUser(user)
//                .orElseThrow(UserExceptions.NOT_FOUND::get);
//
//        // 빌더 패턴을 사용하여 DTO 생성
//        return UserHealthInfoDTO.builder()
//                .diseases(userDiseases.stream()
//                        .map(UserDiseaseEntity::getDisease)
//                        .collect(Collectors.toList()))
//                .pastDiseases(userPastDiseases.stream()
//                        .map(UserPastDiseaseEntity::getDisease)
//                        .collect(Collectors.toList()))
//                .familyDiseases(userFamilyDiseases.stream()
//                        .map(FamilyHistoryEntity::getDisease)
//                        .collect(Collectors.toList()))
//                .allergyMedications(userAllergies.stream()
//                        .map(AllergyEntity::getMedication)
//                        .collect(Collectors.toList()))
//                .build();
//    }
//
//
//
//    @Transactional
//    public String updateUserDisease(UserEntity user, UserHealthInfoDTO userHealthInfoDTO) {
//
//        List<DiseaseEntity> inputDiseaseIds = userHealthInfoDTO.getDiseases();
//        Optional<List<UserDiseaseEntity>> optionalUserDiseases = userDiseaseRepository.findByUser(user);
//        List<UserDiseaseEntity> userDiseases = optionalUserDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);
//
//// 현재 질병 테이블에서 제거해야 할 질병 ID 목록
//        List<UserDiseaseEntity> diseasesToRemove = new ArrayList<>();
//
//        for (UserDiseaseEntity currentDisease : userDiseases) {
//            if (!inputDiseaseIds.contains(currentDisease.getDisease().getDiseaseId())) {
//                diseasesToRemove.add(currentDisease);
//            }
//        }
//        for (UserDiseaseEntity diseaseToRemove : diseasesToRemove) {
//            diseaseToRemove.setUser(null); // `user_profile_id`를 null로 설정
//        }
//        userDiseaseRepository.deleteAll(diseasesToRemove); // `user_profile`과의 관계를 끊은 후 삭제
//
//
//// 새로운 질병 ID 추가
//        List<UserDiseaseEntity> newUserDiseases = new ArrayList<>();
//
//
//        for (DiseaseEntity diseaseId : inputDiseaseIds) {
//            if (userDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(diseaseId))) {
//                UserDiseaseEntity userDisease = UserDiseaseEntity.builder().user(user).disease(new DiseaseEntity(diseaseId.getDiseaseId())).build();
//                newUserDiseases.add(userDisease);
//            }
//        }
//
//        userDiseaseRepository.saveAll(newUserDiseases);
//
//
//        return "UserDisease info updated successfully";
//
//    }
//
//    @Transactional
//    public String updateUserPastDisease(UserEntity user, UserHealthInfoDTO userHealthInfoDTO) {
//
//
//        Optional<List<UserPastDiseaseEntity>> optionalUserPastDiseases = userPastDiseaseRepository.findByUser(user);
//
//        List<DiseaseEntity> inputPastDiseaseIds = userHealthInfoDTO.getPastDiseases();
//
//        List<UserPastDiseaseEntity> userPastDiseases = optionalUserPastDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);
//// 현재 질병 테이블에서 제거해야 할 질병 ID 목록
//
//        List<UserPastDiseaseEntity> pastDiseasesToRemove = new ArrayList<>();
//
//        for (UserPastDiseaseEntity currentPastDisease : userPastDiseases) {
//            if (!inputPastDiseaseIds.contains(currentPastDisease.getDisease().getDiseaseId())) {
//                pastDiseasesToRemove.add(currentPastDisease);
//            }
//        }
//        for (UserPastDiseaseEntity diseaseToRemove : pastDiseasesToRemove) {
//            diseaseToRemove.setUser(null); // `user_profile_id`를 null로 설정
//        }
//        userPastDiseaseRepository.deleteAll(pastDiseasesToRemove);
//
//
//// 새로운 질병 ID 추가
//
//        List<UserPastDiseaseEntity> newUserPastDiseases = new ArrayList<>();
//
//
//        for (DiseaseEntity pastDiseaseId : inputPastDiseaseIds) {
//            if (userPastDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(pastDiseaseId))) {
//                UserPastDiseaseEntity userPastDisease = UserPastDiseaseEntity.builder().user(user).disease(new DiseaseEntity(pastDiseaseId.getDiseaseId())).build();
//                newUserPastDiseases.add(userPastDisease);
//            }
//        }
//
//        userPastDiseaseRepository.saveAll(newUserPastDiseases);
//
//
//        return "UserPastDisease info updated successfully";
//
//
//    }
//
//    @Transactional
//    public String updateUserFamilyDisease(UserEntity user, UserHealthInfoDTO userHealthInfoDTO) {
//
//
//        List<DiseaseEntity> inputFamilyDiseaseIds = userHealthInfoDTO.getFamilyDiseases();
//        Optional<List<FamilyHistoryEntity>> optionalUserFamilyDiseases = familyHistoryRepository.findByUser(user);
//        List<FamilyHistoryEntity> userFamilyDiseases = optionalUserFamilyDiseases.orElseThrow(UserExceptions.NOT_FOUND::get);
//
//
//// 현재 질병 테이블에서 제거해야 할 질병 ID 목록
//        List<FamilyHistoryEntity> familyDiseasesToRemove = new ArrayList<>();
//
//        for (FamilyHistoryEntity currentFamilyDisease : userFamilyDiseases) {
//            if (!inputFamilyDiseaseIds.contains(currentFamilyDisease.getDisease().getDiseaseId())) {
//                familyDiseasesToRemove.add(currentFamilyDisease);
//            }
//        }
//        for (FamilyHistoryEntity diseaseToRemove : familyDiseasesToRemove) {
//            diseaseToRemove.setUser(null); // `user_profile_id`를 null로 설정
//        }
//        familyHistoryRepository.deleteAll(familyDiseasesToRemove);
//
//
//// 새로운 질병 ID 추가
//        List<FamilyHistoryEntity> newFamilyDiseases = new ArrayList<>();
//
//
//        for (DiseaseEntity familyDiseaseId : inputFamilyDiseaseIds) {
//            if (userFamilyDiseases.stream().noneMatch(disease -> disease.getDisease().getDiseaseId().equals(familyDiseaseId))) {
//                FamilyHistoryEntity familyDisease = FamilyHistoryEntity.builder().user(user).disease(new DiseaseEntity(familyDiseaseId.getDiseaseId())).build();
//                newFamilyDiseases.add(familyDisease);
//            }
//        }
//
//        familyHistoryRepository.saveAll(newFamilyDiseases);
//
//        return "UserFamilyDisease info updated successfully";
//
//
//    }
//
//    @Transactional
//    public String updateUserAllergy(UserEntity user, UserHealthInfoDTO userHealthInfoDTO) {
//
//        Optional<List<AllergyEntity>> optionalUserAllergies = allergyRepository.findByUser(user);
//
//        List<AllergyEntity> userAllergyMedications = optionalUserAllergies.orElseThrow(UserExceptions.NOT_FOUND::get);
//
//
//        List<MedicationEntity> inputAllergyMedicationsIds = userHealthInfoDTO.getAllergyMedications();
//
//        List<AllergyEntity> allergyMedicationsToRemove = new ArrayList<>();
//
//        for (AllergyEntity currentAllergyMedication : userAllergyMedications) {
//            MedicationEntity currentMedication = currentAllergyMedication.getMedication();
//            if (currentMedication != null && !inputAllergyMedicationsIds.contains(currentMedication)) {
//                allergyMedicationsToRemove.add(currentAllergyMedication);
//            }
//        }
//
//        for (AllergyEntity medicationToRemove : allergyMedicationsToRemove) {
//            medicationToRemove.setUser(null); // `user_profile_id`를 null로 설정
//        }
//        allergyRepository.deleteAll(allergyMedicationsToRemove);
//
//        // 알레르기 목록에서 제거할 항목을 삭제
//        if (!allergyMedicationsToRemove.isEmpty()) {
//            allergyRepository.deleteAll(allergyMedicationsToRemove);
//        }
//
//
//        // 새로운 질병 ID 추가
//        List<AllergyEntity> newAllergyMedications = new ArrayList<>();
//
//        for (MedicationEntity inputMedication : inputAllergyMedicationsIds) {
//            if (inputMedication != null && userAllergyMedications.stream().noneMatch(existingAllergy -> existingAllergy.getMedication() != null && existingAllergy.getMedication().getMedicationId().equals(inputMedication.getMedicationId()))) {
//                AllergyEntity newAllergyMedication = AllergyEntity.builder().user(user).medication(inputMedication).build();
//                newAllergyMedications.add(newAllergyMedication);
//            }
//        }
//
//        // 새로운 알레르기 데이터를 저장
//        if (!newAllergyMedications.isEmpty()) {
//            allergyRepository.saveAll(newAllergyMedications);
//        }
//
//        return "User allergy information updated successfully";
//
//    }
//
//
//}
