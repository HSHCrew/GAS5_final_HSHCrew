package org.zerock.Altari.Kakao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/altari")
public class KakaoMessageController {

    @Autowired
    private KakaoMessageService kakaoMessageService;

    @GetMapping("/kakao/send")
    public String sendMessage() {
        kakaoMessageService.sendKakaoMessage();
        return "메시지 전송 요청 완료";
    }
}
