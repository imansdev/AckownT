package com.imansdev.ackownt.dto;

import java.time.LocalDate;

public class TransactionDTO {
    private String transactionName;
    private String transactionStatus;
    private Long amount;
    private String trackingNumber;
    private LocalDate transactionDate;
    private String description;
    private Long withdrawalBalance;

    public TransactionDTO(String transactionName, String transactionStatus, Long amount,
            String trackingNumber, LocalDate transactionDate, String description,
            Long withdrawalBalance) {
        this.transactionName = transactionName;
        this.transactionStatus = transactionStatus;
        this.amount = amount;
        this.trackingNumber = trackingNumber;
        this.transactionDate = transactionDate;
        this.description = description;
        this.withdrawalBalance = withdrawalBalance;
    }

    public String getTransactionName() {
        return transactionName;
    }


    public String getTransactionStatus() {
        return transactionStatus;
    }


    public Long getAmount() {
        return amount;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public Long getWithdrawalBalance() {
        return withdrawalBalance;
    }
}
