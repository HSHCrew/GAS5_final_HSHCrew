package org.zerock.Altari.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/test")
    public String ServerTest() {
        return "Server is running";
    }
}
