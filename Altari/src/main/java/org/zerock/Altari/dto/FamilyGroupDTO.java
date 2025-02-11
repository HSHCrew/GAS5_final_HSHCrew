package org.zerock.Altari.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FamilyGroupDTO {

    private Integer familyGroupId;
    private String familyGroupName;
    private LocalDateTime familyGroupCreatedAt;
    private LocalDateTime familyGroupUpdatedAt;
}
