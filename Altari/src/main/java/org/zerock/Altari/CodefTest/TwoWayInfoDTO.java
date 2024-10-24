package org.zerock.Altari.CodefTest;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoWayInfoDTO {

    private int jobIndex;
    private int threadIndex;
    private String jti;
    private long twoWayTimestamp;
}
