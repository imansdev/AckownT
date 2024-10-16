package com.imansdev.ackownt.exception;

public class ValidationException extends RuntimeException {
    private final String fieldName;

    public ValidationException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
