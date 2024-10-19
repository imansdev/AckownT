package com.imansdev.ackownt.model;

import java.time.LocalDate;
import java.time.Period;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.imansdev.ackownt.enums.Gender;
import com.imansdev.ackownt.enums.MilitaryStatus;
import com.imansdev.ackownt.validation.ValidMilitaryStatus;
import com.imansdev.ackownt.validation.ValidNationalId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@ValidMilitaryStatus
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Password is required")
    @NotNull
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Name is required")
    @NotNull
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Surname is required")
    @NotNull
    @Column(nullable = false)
    private String surname;

    @ValidNationalId
    @Column(unique = true, length = 10, nullable = false)
    private String nationalId;


    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Transient
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @NotNull
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @NotNull
    @Pattern(regexp = "\\d{11}", message = "Phone number must be exactly 11 digits")
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilitaryStatus militaryStatus;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Accounts account;

    public Users() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getNationalId() {
        return nationalId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public int getAge() {
        return Period.between(this.dateOfBirth, LocalDate.now()).getYears();
    }

    public Gender getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public MilitaryStatus getMilitaryStatus() {
        return militaryStatus;
    }

    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setMilitaryStatus(MilitaryStatus militaryStatus) {
        this.militaryStatus = militaryStatus;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Users [id=" + id + ", name=" + name + ", surname=" + surname + ", nationalId="
                + nationalId + ", dateOfBirth=" + dateOfBirth + ", age=" + getAge() + ", gender="
                + gender + ", email=" + email + ", phoneNumber=" + phoneNumber + ", militaryStatus="
                + militaryStatus + "]";
    }

}
