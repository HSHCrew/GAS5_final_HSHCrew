package org.zerock.Altari.exception;

public enum UserExceptions {

    NOT_FOUND("NOT FOUND", 404),
    DUPLICATE("DUPLICATE", 409),
    INVALID("INVALID", 400),
    BAD_CREDENTIALS("BAD_CREDENTIALS", 401);

    private UserTaskException userTaskException;

    UserExceptions(String msg, int code) {
        userTaskException = new UserTaskException(msg, code);
    }

    public UserTaskException get() {
        return userTaskException;
    }
}

