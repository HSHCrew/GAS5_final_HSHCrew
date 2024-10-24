package org.zerock.Altari.CodefTest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecondApiRequestDTO {
    private String organization;
    private String smsAuthNo;           // SMS 인증 번호
    private boolean is2Way;
    private TwoWayInfoDTO twoWayInfo;  // 2-way 정보
}