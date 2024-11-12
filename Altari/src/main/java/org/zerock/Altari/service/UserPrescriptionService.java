package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.PrescriptionDrugDTO;
import org.zerock.Altari.dto.UserPrescriptionDTO;
import org.zerock.Altari.entity.*;
import org.zerock.Altari.exception.UserExceptions;
import org.zerock.Altari.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private PrescriptionDrugRepository prescriptionDrugRepository;
    @Autowired
    private UserPrescriptionRepository userPrescriptionRepository;
    @Autowired
    private MedicationRepository medicationRepository;

    @Transactional
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
                List<PrescriptionDrugEntity> prescriptionDrugs = prescriptionDrugRepository.findByPrescriptionId(userPrescription);

                List<PrescriptionDrugDTO> prescriptionDrugDTOs = new ArrayList<>();
                for (PrescriptionDrugEntity prescriptionDrug : prescriptionDrugs) {
                    PrescriptionDrugDTO prescriptionDrugDTO = PrescriptionDrugDTO.builder()
                            .dailyDosesNumber(prescriptionDrug.getDailyDosesNumber())
                            .medicationDirection(prescriptionDrug.getMedicationDirection())
                            .oneDose(prescriptionDrug.getOneDose())
                            .totalDosingDays(prescriptionDrug.getTotalDosingDays())
                            .MedicationId(prescriptionDrug.getMedicationId())
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

    }






