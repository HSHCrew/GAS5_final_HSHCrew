package org.zerock.Altari.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);  // 상위 클래스인 RuntimeException에 메시지를 전달
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);  // 예외의 원인을 전달할 수 있는 생성자
    }
}
