package org.zerock.Altari.dto;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.zerock.Altari.entity.FamilyGroupEntity;
import org.zerock.Altari.entity.UserEntity;

import java.time.LocalDateTime;

public class FamilyMemberDTO {


    private Integer familyMemberId;
    private Integer familyGroup;
    private String username;
    private String relationship;
    private Boolean onGroupManagement;
    private LocalDateTime familyMemberCreatedAt;
    private LocalDateTime familyMemberUpdatedAt;
}
