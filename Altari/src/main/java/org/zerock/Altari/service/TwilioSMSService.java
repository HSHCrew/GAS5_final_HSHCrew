package org.zerock.Altari.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSMSService {

    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.phone_number}")
    private String twilioPhoneNumber;

    public void sendSMS(String toPhoneNumber, String messageBody) {
        // Twilio 초기화
        Twilio.init(accountSid, authToken);

        // SMS 보내기
        Message message = Message.creator(
                new PhoneNumber(toPhoneNumber), // 수신 전화번호
                new PhoneNumber(twilioPhoneNumber), // 발신 전화번호
                messageBody // 메시지 본문
        ).create();

        System.out.println("SMS sent: " + message.getSid());
    }
}