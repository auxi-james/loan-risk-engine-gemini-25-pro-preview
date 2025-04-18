package com.loanrisk.service;

import com.loanrisk.model.Customer;
import com.loanrisk.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for potential future complexity

import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    // Constructor injection
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional // Good practice for operations that modify data
    public Customer createCustomer(Customer customer) {
        // Basic implementation, could add validation or checks here
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true) // Good practice for read operations
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }
}