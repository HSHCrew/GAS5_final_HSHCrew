package org.zerock.Altari.CodefTest;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecondApiRequestDTO {
    private String organization;
    private String simpleAuth;           // SMS 인증 번호
    private boolean is2Way;
    private TwoWayInfoDTO twoWayInfo;  // 2-way 정보
}