package org.zerock.Altari.sample.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/api/v1/samples")
public class SampleController {

    @GetMapping("/list")
    public ResponseEntity<?> list() {

        log.info("list.......");

        String[] arr = {"AAA","BBB","CCC"};

        return ResponseEntity.ok(arr);
    }

}