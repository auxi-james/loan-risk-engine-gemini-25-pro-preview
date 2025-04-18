package com.loanrisk.service;

import com.loanrisk.model.Customer;

import java.util.Optional;

public interface CustomerService {

    /**
     * Creates a new customer.
     *
     * @param customer The customer data to create.
     * @return The created customer.
     */
    Customer createCustomer(Customer customer);

    /**
     * Retrieves a customer by their ID.
     *
     * @param id The ID of the customer to retrieve.
     * @return An Optional containing the customer if found, or empty otherwise.
     */
    Optional<Customer> getCustomerById(Long id);
}