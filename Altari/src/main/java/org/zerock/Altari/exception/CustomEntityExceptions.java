package org.zerock.Altari.exception;

public enum CustomEntityExceptions {

    NOT_FOUND("NOT FOUND", 404),
    DUPLICATE("DUPLICATE", 409),
    INVALID("INVALID", 400),
    BAD_CREDENTIALS("BAD_CREDENTIALS", 401),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", 403); // 권한이 없을 때 추가

    private CustomEntityTaskException medicationTaskException;

    CustomEntityExceptions(String msg, int code) {
        medicationTaskException = new CustomEntityTaskException(msg, code);
    }

    public CustomEntityTaskException get() {
        return medicationTaskException;
    }
}
