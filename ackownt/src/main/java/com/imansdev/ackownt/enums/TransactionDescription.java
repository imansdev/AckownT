package com.imansdev.ackownt.enums;

public enum TransactionDescription {
    CHARGING_SUCCESSFUL("charging was done successfully"), DEDUCTION_SUCCESSFUL(
            "deduction was done successfully"), CHARGING_FAILED(
                    "charging failed"), DEDUCTION_FAILED("deduction failed");

    private final String message;

    TransactionDescription(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
