package com.loanrisk.repository;

import com.loanrisk.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal; // Import BigDecimal
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer createTestCustomer(String name, int age, String income, int score, String status, String debt) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setAge(age);
        customer.setAnnualIncome(new BigDecimal(income));
        customer.setCreditScore(score);
        customer.setEmploymentStatus(status);
        customer.setExistingDebt(new BigDecimal(debt));
        return customer;
    }

    @Test
    public void whenSaveCustomer_thenFindById() {
        Customer customer = createTestCustomer("John Doe", 40, "80000.00", 750, "Employed", "20000.00");

        Customer savedCustomer = customerRepository.save(customer);
        entityManager.flush(); // Ensure data is persisted for findById

        Optional<Customer> foundCustomerOpt = customerRepository.findById(savedCustomer.getId());

        assertThat(foundCustomerOpt).isPresent();
        Customer foundCustomer = foundCustomerOpt.get();
        assertThat(foundCustomer.getName()).isEqualTo(customer.getName());
        assertThat(foundCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(foundCustomer.getAnnualIncome()).isEqualByComparingTo(customer.getAnnualIncome());
        assertThat(foundCustomer.getCreditScore()).isEqualTo(customer.getCreditScore());
        assertThat(foundCustomer.getEmploymentStatus()).isEqualTo(customer.getEmploymentStatus());
        assertThat(foundCustomer.getExistingDebt()).isEqualByComparingTo(customer.getExistingDebt());
    }

    @Test
    public void whenFindAll_thenReturnCustomerList() {
        Customer customer1 = createTestCustomer("Jane Smith", 35, "65000.00", 680, "Self-employed", "10000.00");
        entityManager.persist(customer1);

        Customer customer2 = createTestCustomer("Peter Jones", 50, "120000.00", 800, "Employed", "5000.00");
        entityManager.persist(customer2);

        entityManager.flush();

        List<Customer> customers = customerRepository.findAll();

        assertThat(customers).hasSize(2).extracting(Customer::getName)
                .containsExactlyInAnyOrder("Jane Smith", "Peter Jones");
    }

    @Test
    public void whenDeleteCustomer_thenNotFound() {
        Customer customer = createTestCustomer("Alice Brown", 28, "50000.00", 620, "Unemployed", "25000.00");
        Customer savedCustomer = entityManager.persistFlushFind(customer);

        customerRepository.deleteById(savedCustomer.getId());
        entityManager.flush(); // Ensure delete is processed

        Optional<Customer> foundCustomerOpt = customerRepository.findById(savedCustomer.getId());

        assertThat(foundCustomerOpt).isNotPresent();
    }
}