package com.loanrisk.repository;

import com.loanrisk.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void whenSaveCustomer_thenFindById() {
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));

        Customer savedCustomer = customerRepository.save(customer);
        entityManager.flush(); // Ensure data is persisted for findById

        Optional<Customer> foundCustomerOpt = customerRepository.findById(savedCustomer.getId());

        assertThat(foundCustomerOpt).isPresent();
        assertThat(foundCustomerOpt.get().getFirstName()).isEqualTo(customer.getFirstName());
        assertThat(foundCustomerOpt.get().getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    public void whenFindAll_thenReturnCustomerList() {
        Customer customer1 = new Customer();
        customer1.setFirstName("Jane");
        customer1.setLastName("Smith");
        customer1.setEmail("jane.smith@example.com");
        customer1.setDateOfBirth(LocalDate.of(1985, 5, 15));
        entityManager.persist(customer1);

        Customer customer2 = new Customer();
        customer2.setFirstName("Peter");
        customer2.setLastName("Jones");
        customer2.setEmail("peter.jones@example.com");
        customer2.setDateOfBirth(LocalDate.of(1992, 8, 20));
        entityManager.persist(customer2);

        entityManager.flush();

        List<Customer> customers = customerRepository.findAll();

        assertThat(customers).hasSize(2).extracting(Customer::getEmail)
                .containsExactlyInAnyOrder("jane.smith@example.com", "peter.jones@example.com");
    }

    @Test
    public void whenDeleteCustomer_thenNotFound() {
        Customer customer = new Customer();
        customer.setFirstName("Alice");
        customer.setLastName("Brown");
        customer.setEmail("alice.brown@example.com");
        customer.setDateOfBirth(LocalDate.of(1988, 3, 10));
        Customer savedCustomer = entityManager.persistFlushFind(customer);

        customerRepository.deleteById(savedCustomer.getId());
        entityManager.flush(); // Ensure delete is processed

        Optional<Customer> foundCustomerOpt = customerRepository.findById(savedCustomer.getId());

        assertThat(foundCustomerOpt).isNotPresent();
    }
}