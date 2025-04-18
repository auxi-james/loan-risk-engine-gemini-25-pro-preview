package com.loanrisk.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate; // Added import for LocalDate

@Entity
@Data // Lombok will generate getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok generates no-args constructor
@AllArgsConstructor // Lombok generates all-args constructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth; // Changed fields to match test

}