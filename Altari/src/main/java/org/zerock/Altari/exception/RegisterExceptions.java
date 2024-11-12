package org.zerock.Altari.exception;

public class RegisterExceptions extends RuntimeException {
    public RegisterExceptions(String message) {
        super(message);

    }

    public static RegisterExceptions userAlreadyExists() {
        return new RegisterExceptions("이미 존재하는 아이디입니다.");
    }

    public static RegisterExceptions registrationFailed() {
        return new RegisterExceptions("회원 가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }



}
