package org.zerock.Altari.Codef;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class EasyCodefToken {

    private static String accessToken;
    private static LocalDateTime lastTokenUpdateTime;

    // 5일(432000초)마다 실행 (크론 표현식: 초, 분, 시, 일, 월, 요일)
    @Scheduled(fixedDelay = 432000000) // 5일마다 실행
    public void updateAccessToken() throws InterruptedException, IOException {
        EasyCodef codef = new EasyCodef();

        // 데모 클라이언트 정보 설정
        codef.setClientInfoForDemo(EasyCodefClientInfo.DEMO_CLIENT_ID, EasyCodefClientInfo.DEMO_CLIENT_SECRET);

        // 퍼블릭 키 설정
        codef.setPublicKey(EasyCodefClientInfo.PUBLIC_KEY);

        // 토큰 요청 및 전역 변수에 업데이트
        accessToken = codef.requestNewToken(EasyCodefServiceType.DEMO);  // 토큰 요청 후 전역 변수에 저장
        lastTokenUpdateTime = LocalDateTime.now();  // 마지막 토큰 발급 시간 기록

        System.out.println("Access Token 발급: "+ accessToken);
        System.out.println("토큰 발급 시간: " + lastTokenUpdateTime);
    }

    // 현재 액세스 토큰 확인
    public String getAccessToken() {
        return accessToken;
    }

    // 마지막 토큰 업데이트 시간 확인
    public LocalDateTime getLastTokenUpdateTime() {
        return lastTokenUpdateTime;
    }
}

