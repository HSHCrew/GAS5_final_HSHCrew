package org.zerock.Altari.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);  // 상위 클래스인 RuntimeException에 메시지를 전달
    }

}
