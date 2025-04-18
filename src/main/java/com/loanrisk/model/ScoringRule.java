package com.loanrisk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String field; // Field on Customer/LoanApplication to check
    private String operator; // e.g., "<", ">", "=="
    @Column(name = "rule_value") // "value" can be a reserved keyword
    private String value; // Store rule value as String, handle conversion in logic
    private Integer riskPoints;
    private Integer priority;
    private Boolean enabled;

}