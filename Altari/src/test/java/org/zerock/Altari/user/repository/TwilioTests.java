package org.zerock.Altari.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zerock.Altari.service.TwilioCallService;

@SpringBootTest
public class TwilioTests {

    @Autowired
    private TwilioCallService twilioSMSService;
    @Test
    public void testSendSMS() {
        // 수신 전화번호와 메시지를 지정하여 테스트
        String toPhoneNumber = "+821054841873"; // 테스트할 수신 전화번호 (Twilio에서 승인된 번호 사용)
        String messageBody = "This is a test SMS from TwilioSMSService.";

        // SMS 보내기
        twilioSMSService.sendCall(toPhoneNumber, messageBody);
    }

    @Test
    public void UserTestSendSMS() {
        // 수신 전화번호와 메시지를 지정하여 테스트
        String toPhoneNumber = "+821054841873"; // 테스트할 수신 전화번호 (Twilio에서 승인된 번호 사용)
        String messageBody = "This is a test SMS from TwilioSMSService.";

        // SMS 보내기
        twilioSMSService.sendCall(toPhoneNumber, messageBody);
    }
}
