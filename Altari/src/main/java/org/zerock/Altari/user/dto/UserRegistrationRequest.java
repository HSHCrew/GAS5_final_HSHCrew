package org.zerock.Altari.user.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {

    private UserDTO userDTO;
    private UserProfileDTO userProfileDTO;
}
