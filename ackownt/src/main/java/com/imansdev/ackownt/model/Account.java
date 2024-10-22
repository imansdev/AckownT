package com.imansdev.ackownt.model;

import java.security.SecureRandom;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Min;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 10, nullable = false)
    private String accountNumber;

    @Min(value = 10_000L, message = "The balance must be a positive number and above 10_000")
    @Column(nullable = false)
    private Long balance;

    @Column(nullable = false, updatable = false)
    private LocalDate accountCreationDate;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Customer user;

    private static final SecureRandom secureRandom = new SecureRandom();

    @PrePersist
    public void prePersist() {
        this.accountCreationDate = LocalDate.now();
        this.accountNumber = generateAccountNumber();
    }

    private String generateAccountNumber() {

        long randomNumber = Math.abs(secureRandom.nextLong() % 10_000_000_000L);
        return String.format("%010d", randomNumber);
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Long getBalance() {
        return balance;
    }

    public Customer getUser() {
        return user;
    }

    public LocalDate getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public void setUser(Customer user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Account [id=" + id + ", accountNumber=" + accountNumber + ", balance=" + balance
                + ", accountCreationDate=" + accountCreationDate + ", user=" + user + "]";
    }

}
