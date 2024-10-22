package com.imansdev.ackownt.model;

import java.security.SecureRandom;
import java.time.LocalDate;
import com.imansdev.ackownt.enums.TransactionDescription;
import com.imansdev.ackownt.enums.TransactionStatus;
import com.imansdev.ackownt.enums.TransactionType;
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
public class Transaction {

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
    private TransactionDescription description;

    @Column(nullable = false)
    private Long withdrawalBalance;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Customer user;

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

    public void setTransactionName(TransactionType transactionName) {
        this.transactionName = transactionName;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void setDescription(TransactionDescription description) {
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

    public TransactionDescription getDescription() {
        return description;
    }

    public Long getWithdrawalBalance() {
        return withdrawalBalance;
    }

    public Customer getUser() {
        return user;
    }

    public void setUser(Customer user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Transaction [id=" + id + ", transactionName=" + transactionName
                + ", transactionStatus=" + transactionStatus + ", amount=" + amount
                + ", trackingNumber=" + trackingNumber + ", transactionDate=" + transactionDate
                + ", description=" + description + ", withdrawalBalance=" + withdrawalBalance
                + ", user=" + user + "]";
    }


}
