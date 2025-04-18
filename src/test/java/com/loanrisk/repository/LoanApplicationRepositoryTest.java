package com.loanrisk.repository;

import com.loanrisk.model.Customer;
import com.loanrisk.model.LoanApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LoanApplicationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private CustomerRepository customerRepository; // Needed to create a customer first

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        // Create and persist a customer before each test
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("test.user@example.com");
        customer.setDateOfBirth(LocalDate.of(1995, 6, 20));
        testCustomer = entityManager.persistFlushFind(customer);
    }

    @Test
    public void whenSaveLoanApplication_thenFindById() {
        LoanApplication loanApp = new LoanApplication();
        loanApp.setCustomer(testCustomer);
        loanApp.setLoanAmount(new BigDecimal("10000.00")); // Corrected field name
        loanApp.setLoanPurpose("Home Improvement"); // Corrected field name
        loanApp.setDecision("PENDING"); // Corrected field name (assuming status maps to decision)
        // createdAt is set automatically by @CreationTimestamp, no need to set manually
        // loanApp.setCreatedAt(LocalDateTime.now());

        LoanApplication savedLoanApp = loanApplicationRepository.save(loanApp);
        entityManager.flush();

        Optional<LoanApplication> foundLoanAppOpt = loanApplicationRepository.findById(savedLoanApp.getId());

        assertThat(foundLoanAppOpt).isPresent();
        assertThat(foundLoanAppOpt.get().getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(foundLoanAppOpt.get().getLoanAmount()).isEqualByComparingTo(new BigDecimal("10000.00")); // Corrected field name
        assertThat(foundLoanAppOpt.get().getDecision()).isEqualTo("PENDING"); // Corrected field name
    }

    @Test
    public void whenFindAll_thenReturnLoanApplicationList() {
        LoanApplication loanApp1 = new LoanApplication();
        loanApp1.setCustomer(testCustomer);
        loanApp1.setLoanAmount(new BigDecimal("5000.00")); // Corrected field name
        loanApp1.setLoanPurpose("Car Purchase"); // Corrected field name
        loanApp1.setDecision("APPROVED"); // Corrected field name
        // createdAt is set automatically
        // loanApp1.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persist(loanApp1);

        // Create another customer for the second loan
        Customer customer2 = new Customer();
        customer2.setFirstName("Another");
        customer2.setLastName("Tester");
        customer2.setEmail("another.tester@example.com");
        customer2.setDateOfBirth(LocalDate.of(1980, 1, 1));
        Customer savedCustomer2 = entityManager.persistFlushFind(customer2);


        LoanApplication loanApp2 = new LoanApplication();
        loanApp2.setCustomer(savedCustomer2);
        loanApp2.setLoanAmount(new BigDecimal("20000.00")); // Corrected field name
        loanApp2.setLoanPurpose("Business Startup"); // Corrected field name
        loanApp2.setDecision("REJECTED"); // Corrected field name
        // createdAt is set automatically
        // loanApp2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(loanApp2);

        entityManager.flush();

        List<LoanApplication> loanApps = loanApplicationRepository.findAll();

        assertThat(loanApps).hasSize(2);
        assertThat(loanApps).extracting(LoanApplication::getLoanPurpose) // Corrected field name
                .containsExactlyInAnyOrder("Car Purchase", "Business Startup");
    }

    @Test
    public void whenDeleteLoanApplication_thenNotFound() {
        LoanApplication loanApp = new LoanApplication();
        loanApp.setCustomer(testCustomer);
        loanApp.setLoanAmount(new BigDecimal("1500.00")); // Corrected field name
        loanApp.setLoanPurpose("Vacation"); // Corrected field name
        loanApp.setDecision("PENDING"); // Corrected field name
        // createdAt is set automatically
        // loanApp.setCreatedAt(LocalDateTime.now());
        LoanApplication savedLoanApp = entityManager.persistFlushFind(loanApp);

        loanApplicationRepository.deleteById(savedLoanApp.getId());
        entityManager.flush();

        Optional<LoanApplication> foundLoanAppOpt = loanApplicationRepository.findById(savedLoanApp.getId());

        assertThat(foundLoanAppOpt).isNotPresent();
    }
}