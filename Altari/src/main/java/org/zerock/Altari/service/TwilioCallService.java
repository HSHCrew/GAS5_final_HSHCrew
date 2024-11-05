package org.zerock.Altari.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioCallService {

    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.phone_number}")
    private String twilioPhoneNumber;

    public void sendCall(String toPhoneNumber, String messageBody) {
//        // Twilio 초기화
//        Twilio.init(accountSid, authToken);
//
//        // TwiML을 사용하여 전화를 걸기
//        String twiml = "<Response><Say language=\"ko-KR\" voice=\"Seoyeon\">" + messageBody + "</Say></Response>"; // "alice"는 여성 음성을 의미합니다.
//
//        // 전화 걸기
//        Call call = Call.creator(
//                new PhoneNumber(toPhoneNumber), // 수신 전화번호
//                new PhoneNumber(twilioPhoneNumber), // 발신 전화번호
//                new com.twilio.type.Twiml(twiml) // TwiML 메시지 내용
//        ).create();
//
//        System.out.println("Call SID: " + call.getSid());
        Twilio.init(accountSid, authToken);

        // 문자 메시지 전송
        Message message = Message.creator(
                new PhoneNumber(toPhoneNumber), // 수신 전화번호
                new PhoneNumber(twilioPhoneNumber), // 발신 전화번호
                messageBody // 메시지 내용
        ).create();

        System.out.println("Message SID: " + message.getSid());
    }

}