package com.imansdev.ackownt.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Transactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
