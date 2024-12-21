package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserMedicationDTO;
import org.zerock.Altari.dto.UserPrescriptionDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
@CacheConfig(cacheNames = "userPrescription")
public class UserPrescriontionService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private UserMedicationRepository prescriptionDrugRepository;
    @Autowired
    private UserPrescriptionRepository userPrescriptionRepository;
    @Autowired
    private MedicationRepository medicationRepository;
    @Autowired
    private UserMedicationRepository userMedicationRepository;

    @Transactional(readOnly = true)
    @Cacheable(key = "#username")
    public List<UserPrescriptionDTO> getUserPrescription(UserEntity user) {

        // 여러 처방전을 처리할 수 있도록 findByUserProfile이 반환하는 타입을 List로 변경
        Optional<List<UserPrescriptionEntity>> optionalUserPrescriptions = userPrescriptionRepository.findByUser(user);
        List<UserPrescriptionEntity> userPrescriptions = optionalUserPrescriptions.orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        List<UserPrescriptionDTO> userPrescriptionDTOs = new ArrayList<>();

        for (UserPrescriptionEntity userPrescription : userPrescriptions) {
            // 각 처방전에 대해 약 리스트를 가져옵니다
            Optional<List<UserMedicationEntity>> optionalPrescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(userPrescription);
            List<UserMedicationEntity> prescriptionDrugs = optionalPrescriptionDrugs.orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

            List<UserMedicationDTO> prescriptionDrugDTOs = new ArrayList<>();
            for (UserMedicationEntity prescriptionDrug : prescriptionDrugs) {
                UserMedicationDTO prescriptionDrugDTO = UserMedicationDTO.builder().dailyDosesNumber(prescriptionDrug.getDailyDosesNumber()).medicationDirection(prescriptionDrug.getMedicationDirection()).oneDose(prescriptionDrug.getOneDose()).totalDosingDays(prescriptionDrug.getTotalDosingDays()).Medication(prescriptionDrug.getMedication()).prescriptionId(userPrescription.getUserPrescriptionId()).build();

                prescriptionDrugDTOs.add(prescriptionDrugDTO);
            }

            // 각 처방전과 해당 처방전의 약 리스트를 UserPrescriptionDTO로 매핑
            UserPrescriptionDTO userPrescriptionDTO = UserPrescriptionDTO.builder().userPrescriptionId(userPrescription.getUserPrescriptionId()).commBrandName(userPrescription.getCommBrandName()).manufactureDate(userPrescription.getManufactureDate()).prescriptionNo(userPrescription.getPrescribeNo()).prescriptionOrg(userPrescription.getPrescribeOrg()).telNo(userPrescription.getTelNo()).telNo1(userPrescription.getTelNo2()).isTaken(userPrescription.getIsTaken()).aiSummary(userPrescription.getAiSummary()).prescriptionInfo(userPrescription.getPrescriptionInfo()).drugs(prescriptionDrugDTOs).totalDosingDay(userPrescription.getTotalDosingDay()).build();

            userPrescriptionDTOs.add(userPrescriptionDTO);
        }

        return userPrescriptionDTOs;
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#userPrescriptionId")
    public UserPrescriptionDTO getPrescription(UserEntity user, Integer userPrescriptionId) {



        Optional<UserPrescriptionEntity> optionalUserPrescription = userPrescriptionRepository.findByUserPrescriptionId(userPrescriptionId);
        UserPrescriptionEntity userPrescription = optionalUserPrescription.orElseThrow(UserExceptions.NOT_FOUND::get);
        // optionalUserPrescription 이 null 일 경우 예외 메시지 반환

        Optional<List<UserMedicationEntity>> optionalUserMedications = userMedicationRepository.findByPrescriptionId(userPrescription);
        List<UserMedicationEntity> userMedications = optionalUserMedications.orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        List<UserMedicationDTO> userMedicationDTOS = new ArrayList<>();

        UserPrescriptionDTO userPrescriptionDTO = UserPrescriptionDTO.builder().userPrescriptionId(userPrescription.getUserPrescriptionId()).commBrandName(userPrescription.getCommBrandName()).manufactureDate(userPrescription.getManufactureDate()).prescriptionNo(userPrescription.getPrescribeNo()).prescriptionOrg(userPrescription.getPrescribeOrg()).telNo(userPrescription.getTelNo()).telNo1(userPrescription.getTelNo2()).isTaken(userPrescription.getIsTaken()).aiSummary(userPrescription.getAiSummary()).prescriptionInfo(userPrescription.getPrescriptionInfo()).drugs(userMedicationDTOS).build();

        for (UserMedicationEntity userMedication : userMedications) {
            UserMedicationDTO userMedicationDTO = UserMedicationDTO.builder().dailyDosesNumber(userMedication.getDailyDosesNumber()).medicationDirection(userMedication.getMedicationDirection()).oneDose(userMedication.getOneDose()).totalDosingDays(userMedication.getTotalDosingDays()).Medication(userMedication.getMedication()).prescriptionId(userPrescription.getUserPrescriptionId()).build();

            userMedicationDTOS.add(userMedicationDTO);
        }

        if (!userPrescription.getUser().equals(user)) {
            throw new RuntimeException("Access denied: You do not have permission to access this prescription");
        } // 처방전의 사용자와 로그인한 사용자가 일치한지 검증


        return userPrescriptionDTO;

    }

}








