package com.imansdev.ackownt.dto;

import java.time.LocalDate;

public class AccountDTO {
    private String accountNumber;
    private Long balance;
    private LocalDate accountCreationDate;

    public AccountDTO(String accountNumber, Long balance, LocalDate accountCreationDate) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountCreationDate = accountCreationDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Long getBalance() {
        return balance;
    }


    public LocalDate getAccountCreationDate() {
        return accountCreationDate;
    }
}
