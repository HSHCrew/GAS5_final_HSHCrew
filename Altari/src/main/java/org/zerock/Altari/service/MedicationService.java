package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.MedicationNameImageDTO;
import org.zerock.Altari.dto.TimedMedicationDTO;
import org.zerock.Altari.entity.MedicationEntity;
import org.zerock.Altari.entity.UserMedicationEntity;
import org.zerock.Altari.entity.UserPrescriptionEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.MedicationRepository;

import jakarta.persistence.EntityNotFoundException;
import org.zerock.Altari.repository.UserMedicationRepository;
import org.zerock.Altari.repository.UserPrescriptionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = "medication")
@Log4j2
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
    @Cacheable(key = "'allMedication'")
    public List<MedicationEntity> getAllDrugs() {
        List<MedicationEntity> drugs = medicationRepository.findAll();
        if (drugs.isEmpty()) {
            throw CustomEntityExceptions.NOT_FOUND.get();
        }
        return drugs;
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#medicationId")
    public MedicationEntity getDrugInfo(Integer medicationId) {
        Optional<MedicationEntity> optionalMedication = medicationRepository.findByMedicationId(medicationId);

        return optionalMedication.orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#UserPrescriptionId")
    public TimedMedicationDTO getTimedMedicationList(Integer UserPrescriptionId) {

        Optional<UserPrescriptionEntity> optionalUserPrescription = userPrescriptionRepository.findByUserPrescriptionId(UserPrescriptionId);
        UserPrescriptionEntity userPrescription = optionalUserPrescription.orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        Optional<List<UserMedicationEntity>> optionalTimedMedicationList = userMedicationRepository.findByPrescriptionId(userPrescription);
        List<UserMedicationEntity> timedMedicationList = optionalTimedMedicationList.orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

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

    @CacheEvict(allEntries = true)
    public void clearAllCache() {
        // 아무 작업도 하지 않음 - 캐시만 초기화
    }
}