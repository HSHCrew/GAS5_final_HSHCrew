package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.UserMedicationDTO;
import org.zerock.Altari.dto.UserPrescriptionDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserPrescriptionService {

    @Autowired
    private UserDiseaseRepository userDiseaseRepository;
    @Autowired
    private UserPastDiseaseRepository userPastDiseaseRepository;
    @Autowired
    private AllergyRepository allergyRepository;
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
    @Cacheable(value = "userPrescriptions", key = "#username")
    public List<UserPrescriptionDTO> getUserPrescription(UserEntity username) {

        Optional<UserProfileEntity> optionalUserProfile = Optional.ofNullable(userProfileRepository.findByUsername(username));
        if (optionalUserProfile.isEmpty()) {
            throw UserExceptions.NOT_FOUND.get();
        }

        try {
            UserProfileEntity userProfile = optionalUserProfile.get();

            // 여러 처방전을 처리할 수 있도록 findByUserProfile이 반환하는 타입을 List로 변경
            List<UserPrescriptionEntity> userPrescriptions = userPrescriptionRepository.findByUserProfile(userProfile);
            List<UserPrescriptionDTO> userPrescriptionDTOs = new ArrayList<>();

            for (UserPrescriptionEntity userPrescription : userPrescriptions) {
                // 각 처방전에 대해 약 리스트를 가져옵니다
                List<UserMedicationEntity> prescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(userPrescription);

                List<UserMedicationDTO> prescriptionDrugDTOs = new ArrayList<>();
                for (UserMedicationEntity prescriptionDrug : prescriptionDrugs) {
                    UserMedicationDTO prescriptionDrugDTO = UserMedicationDTO.builder()
                            .dailyDosesNumber(prescriptionDrug.getDailyDosesNumber())
                            .medicationDirection(prescriptionDrug.getMedicationDirection())
                            .oneDose(prescriptionDrug.getOneDose())
                            .totalDosingDays(prescriptionDrug.getTotalDosingDays())
                            .Medication(prescriptionDrug.getMedication())
                            .prescriptionId(userPrescription.getUserPrescriptionId())
                            .build();

                    prescriptionDrugDTOs.add(prescriptionDrugDTO);
                }

                // 각 처방전과 해당 처방전의 약 리스트를 UserPrescriptionDTO로 매핑
                UserPrescriptionDTO userPrescriptionDTO = UserPrescriptionDTO.builder()
                        .userPrescriptionId(userPrescription.getUserPrescriptionId())
                        .commBrandName(userPrescription.getCommBrandName())
                        .manufactureDate(userPrescription.getManufactureDate())
                        .prescriptionNo(userPrescription.getPrescribeNo())
                        .prescriptionOrg(userPrescription.getPrescribeOrg())
                        .telNo(userPrescription.getTelNo())
                        .telNo1(userPrescription.getTelNo2())
                        .isTaken(userPrescription.getIsTaken())
                        .aiSummary(userPrescription.getAiSummary())
                        .prescriptionInfo(userPrescription.getPrescriptionInfo())
                        .drugs(prescriptionDrugDTOs)
                        .totalDosingDay(userPrescription.getTotalDosingDay())
                        .build();

                userPrescriptionDTOs.add(userPrescriptionDTO);
            }

            return userPrescriptionDTOs;

        } catch (Exception e) {
            log.error("Error get userPrescription", e);
            throw new RuntimeException("Error get userPrescription");
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "Prescriptions", key = "#userPrescriptionId")
    public UserPrescriptionDTO getPrescription(Integer userPrescriptionId) {

        try {
            UserPrescriptionEntity userPrescription = userPrescriptionRepository.findByUserPrescriptionId(userPrescriptionId);
            List<UserMedicationEntity> userMedications = userMedicationRepository.findByPrescriptionId(userPrescription);

            List<UserMedicationDTO> userMedicationDTOS = new ArrayList<>();
            for (UserMedicationEntity userMedication : userMedications) {
                UserMedicationDTO userMedicationDTO = UserMedicationDTO.builder()
                        .dailyDosesNumber(userMedication.getDailyDosesNumber())
                        .medicationDirection(userMedication.getMedicationDirection())
                        .oneDose(userMedication.getOneDose())
                        .totalDosingDays(userMedication.getTotalDosingDays())
                        .Medication(userMedication.getMedication())
                        .prescriptionId(userPrescription.getUserPrescriptionId())
                        .build();

                userMedicationDTOS.add(userMedicationDTO);
            }
            UserPrescriptionDTO userPrescriptionDTO = UserPrescriptionDTO.builder()
                    .userPrescriptionId(userPrescription.getUserPrescriptionId())
                    .commBrandName(userPrescription.getCommBrandName())
                    .manufactureDate(userPrescription.getManufactureDate())
                    .prescriptionNo(userPrescription.getPrescribeNo())
                    .prescriptionOrg(userPrescription.getPrescribeOrg())
                    .telNo(userPrescription.getTelNo())
                    .telNo1(userPrescription.getTelNo2())
                    .isTaken(userPrescription.getIsTaken())
                    .aiSummary(userPrescription.getAiSummary())
                    .prescriptionInfo(userPrescription.getPrescriptionInfo())
                    .drugs(userMedicationDTOS)
                    .build();


            return userPrescriptionDTO;
        } catch (Exception e) {
            log.error("Error get userPrescription", e);
            throw new RuntimeException("Error get userPrescription");
        }
    }

}








