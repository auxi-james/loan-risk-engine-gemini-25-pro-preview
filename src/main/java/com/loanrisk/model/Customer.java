package com.loanrisk.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Using BigDecimal for precision as discussed

@Entity
@Data // Lombok will generate getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok generates no-args constructor
@AllArgsConstructor // Lombok generates all-args constructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private BigDecimal annualIncome; // Reverted field, using BigDecimal
    private Integer creditScore;
    private String employmentStatus; // Reverted field (String for simplicity, requirements mention enum)
    private BigDecimal existingDebt; // Reverted field, using BigDecimal

}