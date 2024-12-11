package org.zerock.Altari.exception;

public class CustomEntityTaskException extends RuntimeException {
    private String msg;
    private int code;

    public CustomEntityTaskException(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }
}
