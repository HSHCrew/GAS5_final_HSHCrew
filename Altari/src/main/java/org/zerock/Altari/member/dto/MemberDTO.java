package org.zerock.Altari.member.dto;

import lombok.*;
import org.zerock.Altari.member.entity.MemberEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MemberDTO {

    private String mid;
    private String mpw;
    private String mname;
    private String email;
    private LocalDateTime joinDate;
    private LocalDateTime modifiedDate;
    private String role;

    public Map<String, Object> getDataMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("mid",mid);
        map.put("mname",mname);
        map.put("email",email);
        map.put("role",role);

        return map;
    }

    public MemberDTO(MemberEntity memberEntity) {
        this.mid = memberEntity.getMid();
        this.mpw = memberEntity.getMpw();
        this.mname = memberEntity.getMname();
        this.email = memberEntity.getEmail();
        this.joinDate = memberEntity.getJoinDate();
        this.modifiedDate = memberEntity.getModifiedDate();
        this.role = memberEntity.getRole();
    }
}
