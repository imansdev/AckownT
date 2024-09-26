package com.imansdev.ackownt.model;

import java.security.SecureRandom;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Min;

@Entity
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;

    @Min(value = 0, message = "Amount must be a positive number")
    @Column(nullable = false)
    private Long amount;

    @Column(unique = true, length = 6)
    private String trackingNumber;

    @Column(nullable = false, updatable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Description description;

    @Column(nullable = false)
    private Long withdrawalBalance;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private static final SecureRandom secureRandom = new SecureRandom();

    @PrePersist
    public void prePersist() {
        this.transactionDate = LocalDate.now();
        this.trackingNumber = generateTrackingNumber();
    }

    private String generateTrackingNumber() {
        long randomNumber = Math.abs(secureRandom.nextInt(1_000_000));
        return String.format("%06d", randomNumber);
    }

    public enum TransactionType {
        CHARGE, DEDUCTION
    }

    public enum TransactionStatus {
        SUCCESSFUL, UNSUCCESSFUL
    }

    public enum Description {
        CHARGING_SUCCESSFUL("charging was done successfully"), DEDUCTION_SUCCESSFUL(
                "deduction was done successfully"), CHARGING_FAILED(
                        "charging failed"), DEDUCTION_FAILED("deduction failed");

        private final String message;

        Description(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public void setTransactionName(TransactionType transactionName) {
        this.transactionName = transactionName;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public void setWithdrawalBalance(Long withdrawalBalance) {
        this.withdrawalBalance = withdrawalBalance;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getTransactionName() {
        return transactionName;
    }

    public TransactionStatus getTransactionStatus() {
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

    public Description getDescription() {
        return description;
    }

    public Long getWithdrawalBalance() {
        return withdrawalBalance;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Transactions [id=" + id + ", transactionName=" + transactionName
                + ", transactionStatus=" + transactionStatus + ", amount=" + amount
                + ", trackingNumber=" + trackingNumber + ", transactionDate=" + transactionDate
                + ", description=" + description + ", withdrawalBalance=" + withdrawalBalance
                + ", user=" + user + "]";
    }


}
