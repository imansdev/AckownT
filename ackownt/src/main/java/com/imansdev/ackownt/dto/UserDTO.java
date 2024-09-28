package com.imansdev.ackownt.dto;

public class UserDTO {
    private String name;
    private String surname;
    private String nationalId;
    private String dateOfBirth;
    private String email;
    private String phoneNumber;
    private String gender;
    private String militaryStatus;

    public UserDTO(String name, String surname, String nationalId, String dateOfBirth, String email,
            String phoneNumber, String gender, String militaryStatus) {
        this.name = name;
        this.surname = surname;
        this.nationalId = nationalId;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.militaryStatus = militaryStatus;
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public String getMilitaryStatus() {
        return militaryStatus;
    }
}
