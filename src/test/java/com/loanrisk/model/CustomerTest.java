package com.loanrisk.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate; // Added import for LocalDate

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Configure H2, JPA, Spring Data, etc. for an integration test
public class CustomerTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveCustomer() {
        // Given - Using the new constructor (id, firstName, lastName, email, dateOfBirth)
        Customer customer = new Customer(
                null, // ID is generated
                "John",
                "Doe",
                "john.doe@example.com",
                LocalDate.of(1989, 1, 15) // Example date
        );

        // When
        Customer savedCustomer = entityManager.persistFlushFind(customer); // Persist, flush, and find

        // Then - Asserting the new fields
        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getFirstName()).isEqualTo("John");
        assertThat(savedCustomer.getLastName()).isEqualTo("Doe");
        assertThat(savedCustomer.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedCustomer.getDateOfBirth()).isEqualTo(LocalDate.of(1989, 1, 15));
    }
}