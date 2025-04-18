package com.loanrisk.repository;

import com.loanrisk.model.Customer;
import com.loanrisk.model.LoanApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Removed unused LocalDate import
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LoanApplicationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    // CustomerRepository is not strictly needed here as we use TestEntityManager
    // @Autowired
    // private CustomerRepository customerRepository;

    private Customer testCustomer;

    // Helper to create customer with correct fields
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


    @BeforeEach
    void setUp() {
        // Create and persist a customer before each test using the helper
        testCustomer = createTestCustomer("Test User Repo", 45, "110000.00", 780, "Employed", "5000.00");
        testCustomer = entityManager.persistFlushFind(testCustomer);
        assertThat(testCustomer.getId()).isNotNull(); // Ensure customer persisted
    }

    @Test
    public void whenSaveLoanApplication_thenFindById() {
        LoanApplication loanApp = new LoanApplication();
        loanApp.setCustomer(testCustomer);
        loanApp.setLoanAmount(new BigDecimal("10000.00"));
        loanApp.setLoanPurpose("Home Improvement");
        loanApp.setDecision("PENDING");
        loanApp.setRequestedTermMonths(24); // Added missing required field for LoanApplication constructor
        // createdAt is set automatically

        LoanApplication savedLoanApp = loanApplicationRepository.save(loanApp);
        entityManager.flush();

        Optional<LoanApplication> foundLoanAppOpt = loanApplicationRepository.findById(savedLoanApp.getId());

        assertThat(foundLoanAppOpt).isPresent();
        assertThat(foundLoanAppOpt.get().getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(foundLoanAppOpt.get().getLoanAmount()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(foundLoanAppOpt.get().getDecision()).isEqualTo("PENDING");
        assertThat(foundLoanAppOpt.get().getRequestedTermMonths()).isEqualTo(24);
    }

    @Test
    public void whenFindAll_thenReturnLoanApplicationList() {
        LoanApplication loanApp1 = new LoanApplication();
        loanApp1.setCustomer(testCustomer);
        loanApp1.setLoanAmount(new BigDecimal("5000.00"));
        loanApp1.setLoanPurpose("Car Purchase");
        loanApp1.setDecision("APPROVED");
        loanApp1.setRequestedTermMonths(12); // Added missing required field
        // createdAt is set automatically
        entityManager.persist(loanApp1);

        // Create another customer for the second loan using the helper
        Customer customer2 = createTestCustomer("Another Tester Repo", 30, "60000.00", 650, "Self-employed", "15000.00");
        Customer savedCustomer2 = entityManager.persistFlushFind(customer2);


        LoanApplication loanApp2 = new LoanApplication();
        loanApp2.setCustomer(savedCustomer2);
        loanApp2.setLoanAmount(new BigDecimal("20000.00"));
        loanApp2.setLoanPurpose("Business Startup");
        loanApp2.setDecision("REJECTED");
        loanApp2.setRequestedTermMonths(60); // Added missing required field
        // createdAt is set automatically
        entityManager.persist(loanApp2);

        entityManager.flush();

        List<LoanApplication> loanApps = loanApplicationRepository.findAll();

        assertThat(loanApps).hasSize(2);
        assertThat(loanApps).extracting(LoanApplication::getLoanPurpose)
                .containsExactlyInAnyOrder("Car Purchase", "Business Startup");
    }

    @Test
    public void whenDeleteLoanApplication_thenNotFound() {
        LoanApplication loanApp = new LoanApplication();
        loanApp.setCustomer(testCustomer);
        loanApp.setLoanAmount(new BigDecimal("1500.00"));
        loanApp.setLoanPurpose("Vacation");
        loanApp.setDecision("PENDING");
        loanApp.setRequestedTermMonths(6); // Added missing required field
        // createdAt is set automatically
        LoanApplication savedLoanApp = entityManager.persistFlushFind(loanApp);

        loanApplicationRepository.deleteById(savedLoanApp.getId());
        entityManager.flush();

        Optional<LoanApplication> foundLoanAppOpt = loanApplicationRepository.findById(savedLoanApp.getId());

        assertThat(foundLoanAppOpt).isNotPresent();
    }
}