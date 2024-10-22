package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/register")
@Log4j2
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/join")
    public void joinGET() {

        log.info("joinGET");
    }

    @PostMapping("/join")
    public String joinPOST() {

        log.info("joinPOST");

        return "joinPOST";
    }
}
