package org.zerock.Altari.Kakao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class KakaoMessageService {

    private static final String KAKAO_API_URL = "https://kapi.kakao.com/v2/api/talk/memo/default/send";
    private static final String ACCESS_TOKEN = "5kHaznGFA-xcgvfTSl15D34eb128NSTaAAAAAQo8JJkAAAGS5nZfzP8D-j8FVvr5"; // 실제 액세스 토큰으로 대체 필요

    @Autowired
    private RestTemplate restTemplate;

    public void sendKakaoMessage() {
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + ACCESS_TOKEN);

        // 메시지 템플릿 JSON
        String messageTemplate = "{\"object_type\":\"text\",\"text\":\"정해진 시간에 보내는 알림 메시지입니다.\",\"link\":{\"web_url\":\"http://yourapp.com\",\"mobile_web_url\":\"http://yourapp.com\"}}";

        // URL 인코딩
        String encodedMessage = "template_object=" + URLEncoder.encode(messageTemplate, StandardCharsets.UTF_8);

        // HTTP 요청 생성
        HttpEntity<String> requestEntity = new HttpEntity<>(encodedMessage, headers);

        // API 호출
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 응답 처리
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("메시지 전송 성공: " + response.getBody());
            } else {
                System.out.println("메시지 전송 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
        }
    }
}
