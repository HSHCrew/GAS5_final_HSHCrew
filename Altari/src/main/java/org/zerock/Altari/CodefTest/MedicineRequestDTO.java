package org.zerock.Altari.CodefTest;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequestDTO {
    private String organization;
    private String loginType;
    private String loginTypeLevel;
    private String identity;
    private String userName;
    private String phoneNo;
    private String startDate;
    private String telecom;
    private String reqChildYN;
    private String authMethod;       // 추가된 필드
    private String timeout;          // 추가된 필드
    private String secureNoYN;       // 추가된 필드
}

