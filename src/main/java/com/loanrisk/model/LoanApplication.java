package com.loanrisk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Use LAZY fetching for performance
    @JoinColumn(name = "customer_id", nullable = false) // Foreign key column, assuming customer is mandatory
    private Customer customer;

    private BigDecimal loanAmount;
    private String loanPurpose;
    private Integer requestedTermMonths;

    @Column(nullable = true) // Explicitly mark as nullable
    private Integer riskScore;

    @Column(nullable = true)
    private String riskLevel;

    @Column(nullable = true)
    private String decision;

    @ElementCollection(fetch = FetchType.LAZY) // Use LAZY fetching
    @CollectionTable(name = "loan_explanation", joinColumns = @JoinColumn(name = "loan_application_id"))
    @Column(name = "explanation_text", nullable = true) // Column name within the collection table
    private List<String> explanation;

    @CreationTimestamp // Automatically set on creation
    private LocalDateTime createdAt;

}