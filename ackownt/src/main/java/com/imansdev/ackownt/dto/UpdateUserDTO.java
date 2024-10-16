package com.imansdev.ackownt.dto;

import com.imansdev.ackownt.enums.MilitaryStatus;

public class UpdateUserDTO {
    private String name;
    private String surname;
    private String phoneNumber;
    private MilitaryStatus militaryStatus;

    public UpdateUserDTO(String name, String surname, String phoneNumber,
            MilitaryStatus militaryStatus) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.militaryStatus = militaryStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public MilitaryStatus getMilitaryStatus() {
        return militaryStatus;
    }

    public void setMilitaryStatus(MilitaryStatus militaryStatus) {
        this.militaryStatus = militaryStatus;
    }
}
