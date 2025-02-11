package org.zerock.Altari.exception;

public enum CustomEntityExceptions {

    NOT_FOUND("NOT FOUND", 404),
    DUPLICATE("DUPLICATE", 409),
    INVALID("INVALID", 400),
    BAD_CREDENTIALS("BAD_CREDENTIALS", 401),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", 403),
    ENTITY_CREATION_LIMIT_REACHED("ENTITY_CREATION_LIMIT_REACHED", 429);

    private CustomEntityTaskException medicationTaskException;

    CustomEntityExceptions(String msg, int code) {
        medicationTaskException = new CustomEntityTaskException(msg, code);
    }

    public CustomEntityTaskException get() {
        return medicationTaskException;
    }
}
