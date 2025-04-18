package com.loanrisk.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate; // Added import
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LoanApplicationTest {

    @Autowired
    private TestEntityManager entityManager;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        // Create and persist a customer before each test - Using new constructor
        testCustomer = new Customer(
                null, "Test", "User", "test.user@example.com", LocalDate.of(1984, 4, 1)
        );
        testCustomer = entityManager.persistFlushFind(testCustomer); // Persist and get managed instance
        assertThat(testCustomer.getId()).isNotNull(); // Ensure customer was persisted
    }

    @Test
    void shouldPersistAndRetrieveLoanApplication() {
        // Given
        List<String> explanations = Arrays.asList("Good credit score", "Stable income");
        LoanApplication application = new LoanApplication(
                null, // ID is generated
                testCustomer, // Associate with the persisted customer
                new BigDecimal("15000.00"),
                "Home Improvement",
                36,
                null, // riskScore initially null
                null, // riskLevel initially null
                null, // decision initially null
                explanations, // explanation list
                null // createdAt is generated
        );

        // When
        LoanApplication savedApplication = entityManager.persistFlushFind(application);

        // Then
        assertThat(savedApplication).isNotNull();
        assertThat(savedApplication.getId()).isNotNull();
        assertThat(savedApplication.getCustomer()).isNotNull();
        assertThat(savedApplication.getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(savedApplication.getCustomer().getFirstName()).isEqualTo("Test"); // Asserting new field
        assertThat(savedApplication.getLoanAmount()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(savedApplication.getLoanPurpose()).isEqualTo("Home Improvement");
        assertThat(savedApplication.getRequestedTermMonths()).isEqualTo(36);
        assertThat(savedApplication.getRiskScore()).isNull();
        assertThat(savedApplication.getRiskLevel()).isNull();
        assertThat(savedApplication.getDecision()).isNull();
        assertThat(savedApplication.getExplanation()).containsExactlyInAnyOrder("Good credit score", "Stable income");
        assertThat(savedApplication.getCreatedAt()).isNotNull();
        assertThat(savedApplication.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now()); // Check timestamp is reasonable
    }

     @Test
    void shouldUpdateLoanApplicationFields() {
        // Given
        LoanApplication application = new LoanApplication(
                null, testCustomer, new BigDecimal("5000"), "Car", 24, null, null, null, null, null
        );
        LoanApplication savedApplication = entityManager.persistFlushFind(application);
        Long savedId = savedApplication.getId();

        // When
        // Detach and find again to simulate fetching later
        entityManager.detach(savedApplication);
        LoanApplication retrievedApplication = entityManager.find(LoanApplication.class, savedId);

        retrievedApplication.setRiskScore(50);
        retrievedApplication.setRiskLevel("Low");
        retrievedApplication.setDecision("Approved");
        retrievedApplication.setExplanation(List.of("Updated explanation"));
        entityManager.persistFlushFind(retrievedApplication); // Save changes

        // Then
        LoanApplication updatedApplication = entityManager.find(LoanApplication.class, savedId);
        assertThat(updatedApplication.getRiskScore()).isEqualTo(50);
        assertThat(updatedApplication.getRiskLevel()).isEqualTo("Low");
        assertThat(updatedApplication.getDecision()).isEqualTo("Approved");
        assertThat(updatedApplication.getExplanation()).containsExactly("Updated explanation");
    }
}