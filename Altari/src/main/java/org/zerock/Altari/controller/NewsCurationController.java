package org.zerock.Altari.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.NewsCurationDTO;
import org.zerock.Altari.entity.ArticleEntity;
import org.zerock.Altari.entity.NewsCurationEntity;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.exception.EntityNotMatchedException;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.NewsCurationService;
import org.zerock.Altari.service.RegisterService;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

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

        // JWT에서 사용자 정보를 추출
        UserEntity userToken = jwtUtil.getUsernameFromToken(accessToken);
        UserEntity user = new UserEntity(username);
        String tokenUsername = userToken.getUsername();
        String entityUsername = user.getUsername();

        // userToken과 user가 다르면 예외 처리
        if (!tokenUsername.equals(entityUsername)) {
            throw new EntityNotMatchedException("권한이 없습니다.");
        }

        // NewsCurationService를 통해 관련된 NewsCurationDTO 목록을 조회
        List<NewsCurationDTO> newsCurationData = newsCurationService.getNewsCurationByUserId(user);

        if (newsCurationData.isEmpty()) {
            return ResponseEntity.noContent().build(); // 결과가 없을 경우 204 No Content 응답
        }

        return ResponseEntity.ok(newsCurationData); // 결과가 있으면 200 OK 응답
    }
}
