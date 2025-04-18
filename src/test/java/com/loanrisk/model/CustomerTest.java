package com.loanrisk.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Configure H2, JPA, Spring Data, etc. for an integration test
public class CustomerTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveCustomer() {
        // Given
        Customer customer = new Customer(
                null, // ID is generated
                "John Doe",
                35,
                new BigDecimal("60000.00"),
                720,
                "Employed",
                new BigDecimal("5000.00")
        );

        // When
        Customer savedCustomer = entityManager.persistFlushFind(customer); // Persist, flush, and find

        // Then
        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getName()).isEqualTo("John Doe");
        assertThat(savedCustomer.getAge()).isEqualTo(35);
        assertThat(savedCustomer.getAnnualIncome()).isEqualByComparingTo(new BigDecimal("60000.00"));
        assertThat(savedCustomer.getCreditScore()).isEqualTo(720);
        assertThat(savedCustomer.getEmploymentStatus()).isEqualTo("Employed");
        assertThat(savedCustomer.getExistingDebt()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }
}