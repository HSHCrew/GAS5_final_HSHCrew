package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.PrescriptionDTO;
import org.zerock.Altari.dto.PrescriptionDrugDTO;
import org.zerock.Altari.dto.UserHealthInfoDTO;
import org.zerock.Altari.dto.UserPrescriptionDTO;
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
                            .medication_direction(prescriptionDrug.getMedication_direction())
                            .one_dose(prescriptionDrug.getOne_dose())
                            .total_dosing_days(prescriptionDrug.getTotal_dosing_days())
                            .medicationId(prescriptionDrug.getMedicationId())
                            .prescription_id(userPrescription.getUser_prescription_id())
                            .build();

                    prescriptionDrugDTOs.add(prescriptionDrugDTO);
                }

                // 각 처방전과 해당 처방전의 약 리스트를 UserPrescriptionDTO로 매핑
                UserPrescriptionDTO userPrescriptionDTO = UserPrescriptionDTO.builder()
                        .user_prescription_id(userPrescription.getUser_prescription_id())
                        .comm_brand_name(userPrescription.getComm_brand_name())
                        .manufacture_date(userPrescription.getManufacture_date())
                        .prescribe_no(userPrescription.getPrescribeNo())
                        .prescribe_org(userPrescription.getPrescribe_org())
                        .tel_no(userPrescription.getTel_no())
                        .tel_no1(userPrescription.getTel_no1())
                        .isTaken(userPrescription.getIsTaken())
                        .ai_summary(userPrescription.getAi_summary())
                        .prescription_info(userPrescription.getPrescription_info())
                        .drugs(prescriptionDrugDTOs)
                        .build();

                userPrescriptionDTOs.add(userPrescriptionDTO);
            }

            return userPrescriptionDTOs;

        } catch (Exception e) {
            log.error("Error get userPrescription", e);
            throw new RuntimeException("Error get userPrescription");
        }
    }

    @Transactional
    public List<UserPrescriptionDTO> calculateMedicationSuccessRate(UserPrescriptionEntity userPrescription) {

        


        return null;


        }
    }






