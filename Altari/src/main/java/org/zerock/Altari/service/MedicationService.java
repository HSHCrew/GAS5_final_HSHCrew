package org.zerock.Altari.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.MedicationDTO;
import org.zerock.Altari.dto.MedicationNameImageDTO;
import org.zerock.Altari.dto.TimedMedicationDTO;
import org.zerock.Altari.dto.UserMedicationDTO;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.UserMedicationEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;
import org.zerock.Altari.repository.MedicationRepository;

import jakarta.persistence.EntityNotFoundException;
import org.zerock.Altari.repository.UserMedicationRepository;
import org.zerock.Altari.repository.UserPrescriptionRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final UserMedicationRepository userMedicationRepository;
    private final UserPrescriptionRepository userPrescriptionRepository;

    @Autowired
    public MedicationService(MedicationRepository medicationRepository, UserMedicationRepository userMedicationRepository, UserPrescriptionRepository userPrescriptionRepository) {
        this.medicationRepository = medicationRepository;
        this.userMedicationRepository = userMedicationRepository;
        this.userPrescriptionRepository = userPrescriptionRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "medications", key = "'all_drugs'") // 모든 약을 캐시 처리
    public List<MedicationEntity> getAllDrugs() {
        List<MedicationEntity> drugs = medicationRepository.findAll();
        if (drugs.isEmpty()) {
            throw new EntityNotFoundException("No medications found in the database");
        }
        return drugs;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "medications", key = "#medicationId") // 특정 약 정보를 캐시 처리
    public MedicationEntity getDrugInfo(Integer medicationId) {
        MedicationEntity medication = medicationRepository.findByMedicationId(medicationId);
        if (medication == null) {
            throw new EntityNotFoundException("No medication found for the given ID");
        }
        return medication;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "medications", key = "#UserPrescriptionId")
    public TimedMedicationDTO getTimedMedicationList(Integer UserPrescriptionId) {
        UserPrescriptionEntity userPrescription = userPrescriptionRepository.findByUserPrescriptionId(UserPrescriptionId);
        List<UserMedicationEntity> timedMedicationList = userMedicationRepository.findByPrescriptionId(userPrescription);

        if (timedMedicationList.isEmpty()) {
            throw new EntityNotFoundException("No medications found for the given ID");
        }



        List<MedicationNameImageDTO> MorningMedicationList = new ArrayList<MedicationNameImageDTO>();
        List<MedicationNameImageDTO> LunchMedicationList = new ArrayList<MedicationNameImageDTO>();
        List<MedicationNameImageDTO> DinnerMedicationList = new ArrayList<MedicationNameImageDTO>();
        List<MedicationNameImageDTO> NightMedicationList = new ArrayList<MedicationNameImageDTO>();

        for (UserMedicationEntity userMedication : timedMedicationList) {

            MedicationEntity medication = userMedication.getMedication();

            MedicationNameImageDTO medicationDTO = MedicationNameImageDTO.builder()
                    .medicationId(medication.getMedicationId())
                    .medicationName(medication.getMedicationName())
                    .itemImage(medication.getItemImage())
                    .oneDose(userMedication.getOneDose())
                    .build();
            if (userMedication.getDailyDosesNumber() == 1) {
                MorningMedicationList.add(medicationDTO);
            }
            if (userMedication.getDailyDosesNumber() == 2) {
                MorningMedicationList.add(medicationDTO);
                DinnerMedicationList.add(medicationDTO);
            }
            if (userMedication.getDailyDosesNumber() == 3) {
                MorningMedicationList.add(medicationDTO);
                LunchMedicationList.add(medicationDTO);
                DinnerMedicationList.add(medicationDTO);
            }
            if (userMedication.getDailyDosesNumber() == 4) {
                MorningMedicationList.add(medicationDTO);
                LunchMedicationList.add(medicationDTO);
                DinnerMedicationList.add(medicationDTO);
                NightMedicationList.add(medicationDTO);
            }

        }

        TimedMedicationDTO timedMedication = TimedMedicationDTO.builder()
                .MorningMedications(MorningMedicationList)
                .LunchMedications(LunchMedicationList)
                .DinnerMedications(DinnerMedicationList)
                .NightMedications(NightMedicationList)
                .build();

        return timedMedication;

    }
}