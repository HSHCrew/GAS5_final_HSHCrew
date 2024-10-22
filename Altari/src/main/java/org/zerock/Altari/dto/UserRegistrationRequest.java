package org.zerock.Altari.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {

    private UserDTO userDTO;
    private UserProfileDTO userProfileDTO;
}
