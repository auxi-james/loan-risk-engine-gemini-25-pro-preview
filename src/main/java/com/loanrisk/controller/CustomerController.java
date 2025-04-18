package com.loanrisk.controller;

import com.loanrisk.model.Customer;
import com.loanrisk.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // For handling not found

import java.util.Optional;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    // Constructor injection
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Creates a new customer.
     * POST /customers
     *
     * @param customer The customer data from the request body.
     * @return The created customer with HTTP status 201 (Created).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer createCustomer(@RequestBody Customer customer) {
        // Consider adding @Valid annotation if validation is set up
        return customerService.createCustomer(customer);
    }

    /**
     * Retrieves a customer by their ID.
     * GET /customers/{id}
     *
     * @param id The ID of the customer to retrieve.
     * @return The customer if found with HTTP status 200 (OK),
     *         or HTTP status 404 (Not Found) if the customer does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customerOptional = customerService.getCustomerById(id);
        if (customerOptional.isPresent()) {
            return ResponseEntity.ok(customerOptional.get());
        } else {
            // Option 1: Return ResponseEntity directly
            return ResponseEntity.notFound().build();
            // Option 2: Throw an exception to be handled globally (requires ExceptionHandler)
            // throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with id: " + id);
        }
    }
}