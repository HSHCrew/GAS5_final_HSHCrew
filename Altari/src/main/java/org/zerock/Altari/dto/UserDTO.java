package org.zerock.Altari.dto;

import lombok.*;
import org.zerock.Altari.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {

    private Integer authId;
    private String username;
    private String password;
    private String role;
    private LocalDateTime user_created_at;
    private LocalDateTime user_updated_at;

    public Map<String, Object> getDataMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("role", role);
        return map;
    }


    public UserEntity toEntity() {
        return UserEntity.builder()
                .username(this.username)
                .password(this.password)
                .role(this.role)
                .build();
    }

    public UserDTO(UserEntity userEntity){
        this.username = userEntity.getUsername();
        this.password = userEntity.getPassword();
        this.role = userEntity.getRole();
   }

}

