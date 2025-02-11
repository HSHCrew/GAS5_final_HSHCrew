package org.zerock.Altari.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.Altari.dto.FamilyGroupDTO;
import org.zerock.Altari.entity.FamilyGroupEntity;
import org.zerock.Altari.entity.FamilyMemberEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.CustomEntityExceptions;
import org.zerock.Altari.repository.FamilyGroupRepository;
import org.zerock.Altari.repository.FamilyMemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class FamilyService {

    private final FamilyGroupRepository familyGroupRepository;
    private final FamilyMemberRepository familyMemberRepository;

    public List<FamilyGroupDTO> readFamilyGroup(UserEntity user) {

        List<FamilyMemberEntity> familyMembers = familyMemberRepository.findDistinctByUser(user)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);

        return familyMembers.stream()
                .map(FamilyMemberEntity::getFamilyGroup)   // FamilyGroupEntity 추출
                .distinct()                                // 중복 제거
                .map(familyGroup -> FamilyGroupDTO.builder() // 바로 DTO로 변환
                        .familyGroupId(familyGroup.getFamilyGroupId())
                        .familyGroupName(familyGroup.getFamilyGroupName())
                        .familyGroupCreatedAt(familyGroup.getFamilyGroupCreatedAt())
                        .familyGroupUpdatedAt(familyGroup.getFamilyGroupUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public FamilyGroupDTO createFamily(FamilyGroupDTO familyGroupDTO, UserEntity user) {

        FamilyGroupEntity familyGroup = FamilyGroupEntity.builder()
                .familyGroupName(familyGroupDTO.getFamilyGroupName())
                .build();

        FamilyGroupEntity createdGroup = familyGroupRepository.save(familyGroup);

        FamilyMemberEntity familyMember = FamilyMemberEntity.builder()
                .familyGroup(createdGroup)
                .user(user)
                .onGroupManagement(true)
                .build();

        familyMemberRepository.save(familyMember);

        return FamilyGroupDTO.builder()
                .familyGroupId(createdGroup.getFamilyGroupId())
                .familyGroupName(createdGroup.getFamilyGroupName())
                .familyGroupCreatedAt(createdGroup.getFamilyGroupCreatedAt())
                .familyGroupUpdatedAt(createdGroup.getFamilyGroupUpdatedAt())
                .build();

    }

    public FamilyGroupDTO updateFamilyGroup(Integer familyGroupId, FamilyGroupDTO familyGroupDTO, UserEntity user) {

        FamilyGroupEntity familyGroup = familyGroupRepository.findByFamilyGroupId(familyGroupId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        FamilyMemberEntity familyMember = familyMemberRepository.findByUserAndFamilyGroup(user, familyGroup)
                .orElseThrow(() -> {
                    log.warn("updateFamilyGroup - 요청자가 해당 그룹에 속하지 않음. 요청자: {}, 그룹 ID: {}", user.getUsername(), familyGroupId);
                    return CustomEntityExceptions.NOT_FOUND.get();
                });

        if (!familyMember.getOnGroupManagement()) {
            log.warn("updateFamilyGroup - 그룹 관리 권한이 없습니다. 요청자: {}", user.getUsername());
            throw CustomEntityExceptions.UNAUTHORIZED_ACCESS.get();
        }

        log.info("updateFamilyGroup - 그룹 관리 권한 확인 완료. familyGroupId: {}", familyGroupId);

        familyGroup.setFamilyGroupName(familyGroupDTO.getFamilyGroupName());

        FamilyGroupEntity updatedGroup = familyGroupRepository.save(familyGroup);

        return FamilyGroupDTO.builder()
                .familyGroupId(updatedGroup.getFamilyGroupId())
                .familyGroupName(updatedGroup.getFamilyGroupName())
                .familyGroupCreatedAt(updatedGroup.getFamilyGroupCreatedAt())
                .familyGroupUpdatedAt(updatedGroup.getFamilyGroupUpdatedAt())
                .build();

    }

    public void deleteFamilyGroup(Integer familyGroupId, UserEntity user) {

        FamilyGroupEntity familyGroup = familyGroupRepository.findByFamilyGroupId(familyGroupId)
                .orElseThrow(CustomEntityExceptions.NOT_FOUND::get);
        FamilyMemberEntity familyMember = familyMemberRepository.findByUserAndFamilyGroup(user, familyGroup)
                .orElseThrow(() -> {
                    log.warn("deleteFamilyGroup - 요청자가 해당 그룹에 속하지 않음. 요청자: {}, 그룹 ID: {}", user.getUsername(), familyGroupId);
                    return CustomEntityExceptions.NOT_FOUND.get();
                });

        if (!familyMember.getOnGroupManagement()) {
            log.warn("deleteFamilyGroup - 그룹 관리 권한이 없습니다. 요청자: {}", user.getUsername());
            throw CustomEntityExceptions.UNAUTHORIZED_ACCESS.get();
        }

        familyGroupRepository.delete(familyGroup);

        log.info("deleteFamilyGroup - 그룹이 성공적으로 삭제되었습니다. 삭제된 그룹 ID: {}, 그룹명: {}, 요청자: {}",
                familyGroupId, familyGroup.getFamilyGroupName(), user.getUsername());

    }
}
