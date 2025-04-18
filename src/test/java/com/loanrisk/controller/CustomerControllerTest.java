package com.loanrisk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanrisk.model.Customer;
import com.loanrisk.repository.CustomerRepository; // Import repository for potential cleanup/setup
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional; // Ensure tests run in transactions

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // Loads the full application context
@AutoConfigureMockMvc // Configures MockMvc
@Transactional // Rollback transactions after each test
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    @Autowired
    private CustomerRepository customerRepository; // For setup/verification if needed

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        // Clean up before each test if necessary, though @Transactional handles most cases
        // customerRepository.deleteAll();

        testCustomer = new Customer();
        // Corrected to use the 'name' field and BigDecimal for income
        testCustomer.setName("Test User");
        testCustomer.setCreditScore(750);
        testCustomer.setAnnualIncome(new java.math.BigDecimal("60000.00"));
        // Add other relevant fields if needed for the test, e.g., age, employmentStatus
        testCustomer.setAge(30);
        testCustomer.setEmploymentStatus("Employed");
        testCustomer.setExistingDebt(new java.math.BigDecimal("5000.00"));
    }

    @Test
    void createCustomer_shouldReturnCreatedCustomer() throws Exception {
        String customerJson = objectMapper.writeValueAsString(testCustomer);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isCreated()) // Expect HTTP 201
                .andExpect(jsonPath("$.id", notNullValue())) // Expect ID to be generated
                .andExpect(jsonPath("$.name", is("Test User"))) // Corrected field name
                .andExpect(jsonPath("$.creditScore", is(750)))
                .andExpect(jsonPath("$.annualIncome", is(60000.00))) // Corrected value type (number)
                .andExpect(jsonPath("$.age", is(30))) // Added assertion
                .andExpect(jsonPath("$.employmentStatus", is("Employed"))) // Added assertion
                .andExpect(jsonPath("$.existingDebt", is(5000.00))); // Added assertion
    }

    @Test
    void getCustomerById_whenCustomerExists_shouldReturnCustomer() throws Exception {
        // Arrange: Save a customer first
        Customer savedCustomer = customerRepository.save(testCustomer);
        Long customerId = savedCustomer.getId();

        // Act & Assert
        mockMvc.perform(get("/customers/{id}", customerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect HTTP 200
                .andExpect(jsonPath("$.id", is(customerId.intValue()))) // Compare ID
                .andExpect(jsonPath("$.name", is(testCustomer.getName())))
                .andExpect(jsonPath("$.creditScore", is(testCustomer.getCreditScore())))
                .andExpect(jsonPath("$.annualIncome", is(testCustomer.getAnnualIncome().doubleValue()))); // Compare BigDecimal as double
                // Add other field assertions as needed
    }

    @Test
    void getCustomerById_whenCustomerDoesNotExist_shouldReturnNotFound() throws Exception {
        // Arrange: Use an ID that is unlikely to exist
        Long nonExistentId = 9999L;

        // Act & Assert
        mockMvc.perform(get("/customers/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Expect HTTP 404
    }


    // Tests for GET /customers/{id} will be added next
}