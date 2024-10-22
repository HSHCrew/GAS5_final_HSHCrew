package org.zerock.Altari.user.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.Altari.user.dto.UserDTO;
import org.zerock.Altari.user.dto.UserProfileDTO;
import org.zerock.Altari.user.dto.UserRegistrationRequest;
import org.zerock.Altari.user.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class RegisterController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegistrationRequest userRegistrationRequest) {
    UserDTO userDTO = userRegistrationRequest.getUserDTO();
    UserProfileDTO userProfileDTO = userRegistrationRequest.getUserProfileDTO();
    userService.registerUser(userDTO, userProfileDTO);
    return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
