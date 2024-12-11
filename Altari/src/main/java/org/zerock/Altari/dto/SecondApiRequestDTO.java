package org.zerock.Altari.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.entity.UserProfileEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecondApiRequestDTO {
    private String organization;
    private String simpleAuth;
    @JsonProperty("is2Way")// SMS 인증 번호
    private boolean is2Way;
    private TwoWayInfoDTO twoWayInfo;  // 2-way 정보
    private UserEntity user;

}