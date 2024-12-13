package org.zerock.Altari.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.NewsCurationDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.NewsCurationService;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/altari")
@Log4j2
@RequiredArgsConstructor
public class NewsCurationController {

    @Autowired
    private final NewsCurationService newsCurationService;
    private final JWTUtil jwtUtil;

    // 사용자 ID를 받아서 NewsCurationEntity와 관련된 ArticleEntity 목록을 반환하는 API
    @GetMapping("/newsCuration/{username}")
    public ResponseEntity<List<NewsCurationDTO>> getNewsCurationByUserId(@PathVariable("username") String username,
                                                                         @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {

        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        List<NewsCurationDTO> newsCurationData = newsCurationService.getNewsCurationByUserId(user);

        return ResponseEntity.ok(newsCurationData); // 결과가 있으면 200 OK 응답
    }
}
