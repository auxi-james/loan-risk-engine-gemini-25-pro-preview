package com.loanrisk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanrisk.dto.LoanApplicationRequest;
import com.loanrisk.dto.LoanApplicationResponse;
import com.loanrisk.model.Customer;
import com.loanrisk.model.LoanApplication;
import com.loanrisk.model.ScoringRule;
import com.loanrisk.repository.CustomerRepository;
import com.loanrisk.repository.LoanApplicationRepository;
import com.loanrisk.repository.ScoringRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional; // Important for lazy loading and cleanup

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback transactions after each test
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private ScoringRuleRepository scoringRuleRepository;

    private Customer testCustomer;
    private ScoringRule ruleLowIncome;
    private ScoringRule ruleHighLoanAmount;

    @BeforeEach
    void setUp() {
        // Clean up previous data to ensure test isolation
        loanApplicationRepository.deleteAll();
        scoringRuleRepository.deleteAll();
        customerRepository.deleteAll();

        // Create Test Customer
        // Create Test Customer
        testCustomer = new Customer();
        testCustomer.setName("Test User"); // Use 'setName'
        testCustomer.setAge(30); // Add age
        testCustomer.setAnnualIncome(BigDecimal.valueOf(50000));
        testCustomer.setCreditScore(700);
        testCustomer.setEmploymentStatus("Employed"); // Add employment status
        testCustomer.setExistingDebt(BigDecimal.valueOf(5000)); // Add existing debt
        testCustomer = customerRepository.save(testCustomer);

        // Create Test Scoring Rules
        ruleLowIncome = new ScoringRule();
        ruleLowIncome.setName("Low Income Rule"); // Use 'setName'
        ruleLowIncome.setField("annualIncome"); // Use 'setField'
        ruleLowIncome.setOperator("<"); // Use 'setOperator'
        ruleLowIncome.setValue("30000"); // Use 'setValue'
        ruleLowIncome.setRiskPoints(-200); // Use 'setRiskPoints'
        ruleLowIncome.setPriority(1); // Add priority
        ruleLowIncome.setEnabled(true);
        // Decision and Explanation are outcomes of the service, not stored directly on the rule entity itself
        ruleLowIncome = scoringRuleRepository.save(ruleLowIncome);

        ruleHighLoanAmount = new ScoringRule();
        ruleHighLoanAmount.setName("High Loan Amount Rule"); // Use 'setName'
        ruleHighLoanAmount.setField("loanAmount"); // Use 'setField'
        ruleHighLoanAmount.setOperator(">"); // Use 'setOperator'
        ruleHighLoanAmount.setValue("100000"); // Use 'setValue'
        ruleHighLoanAmount.setRiskPoints(-50); // Use 'setRiskPoints'
        ruleHighLoanAmount.setPriority(2); // Add priority
        ruleHighLoanAmount.setEnabled(true);
        // Decision and Explanation are outcomes of the service, not stored directly on the rule entity itself
        ruleHighLoanAmount = scoringRuleRepository.save(ruleHighLoanAmount);
    }
    @Test
    void applyForLoan_Success_ShouldCreateApplicationAndReturnResponse() throws Exception {
        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setCustomerId(testCustomer.getId());
        request.setLoanAmount(10000.0); // Below high loan amount rule
        request.setLoanPurpose("Home Improvement");
        request.setRequestedTermMonths(36);

        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/loan/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanId").isNumber())
                .andExpect(jsonPath("$.riskScore").value(500)) // Base score
                .andExpect(jsonPath("$.riskLevel").value("Medium")) // Corrected casing and logic (500 is Medium)
                .andExpect(jsonPath("$.decision").value("MANUAL_REVIEW")) // Expect uppercase
                .andExpect(jsonPath("$.explanation").isEmpty()) // No rules triggered
                .andReturn();

        // Verify response DTO
        String responseString = result.getResponse().getContentAsString();
        LoanApplicationResponse response = objectMapper.readValue(responseString, LoanApplicationResponse.class);
        assertThat(response.getLoanId()).isNotNull();

        // Verify persistence
        LoanApplication savedApp = loanApplicationRepository.findById(response.getLoanId()).orElseThrow();
        assertThat(savedApp.getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(savedApp.getLoanAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000.0));
        assertThat(savedApp.getLoanPurpose()).isEqualTo("Home Improvement");
        assertThat(savedApp.getRequestedTermMonths()).isEqualTo(36);
        assertThat(savedApp.getRiskScore()).isEqualTo(500);
        assertThat(savedApp.getRiskLevel()).isEqualTo("Medium"); // Corrected casing
        assertThat(savedApp.getDecision()).isEqualTo("MANUAL_REVIEW"); // Expect uppercase
        assertThat(savedApp.getExplanation()).isEmpty();
    }

     @Test
    void applyForLoan_TriggeringRule_ShouldReflectInResponse() throws Exception {
        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setCustomerId(testCustomer.getId());
        request.setLoanAmount(150000.0); // Above high loan amount rule
        request.setLoanPurpose("Large Project");
        request.setRequestedTermMonths(60);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/loan/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").isNumber())
                .andExpect(jsonPath("$.riskScore").value(450)) // Base score 500 - 50
                .andExpect(jsonPath("$.riskLevel").value("Medium")) // Corrected logic (450 is Medium)
                .andExpect(jsonPath("$.decision").value("MANUAL_REVIEW")) // Expect uppercase
                .andExpect(jsonPath("$.explanation[0]").value("High Loan Amount Rule")); // Rule explanation matches rule name
    }


    @Test
    void applyForLoan_CustomerNotFound_ShouldReturnNotFound() throws Exception {
        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setCustomerId(999L); // Non-existent customer ID
        request.setLoanAmount(5000.0);
        request.setLoanPurpose("Vacation");
        request.setRequestedTermMonths(12);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/loan/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound()); // Expect 404 Not Found
    }

    @Test
    void getLoanApplication_Success_ShouldReturnApplicationDetails() throws Exception {
        // Arrange: Create and save an application first
        LoanApplication app = new LoanApplication();
        app.setCustomer(testCustomer);
        app.setLoanAmount(BigDecimal.valueOf(25000.0));
        app.setLoanPurpose("Car Purchase");
        app.setRequestedTermMonths(48);
        app.setRiskScore(550); // Example score
        app.setRiskLevel("Medium"); // Correct casing
        app.setDecision("MANUAL_REVIEW"); // Expect uppercase (Medium -> MANUAL_REVIEW)
        app.setExplanation(List.of("Some explanation")); // Example explanation
        app = loanApplicationRepository.save(app);
        Long savedId = app.getId();

        // Act & Assert
        mockMvc.perform(get("/loan/{id}", savedId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanId").value(savedId))
                .andExpect(jsonPath("$.riskScore").value(550))
                .andExpect(jsonPath("$.riskLevel").value("Medium")) // Corrected casing
                .andExpect(jsonPath("$.decision").value("MANUAL_REVIEW")) // Expect uppercase
                .andExpect(jsonPath("$.explanation[0]").value("Some explanation"));
    }

    @Test
    void getLoanApplication_NotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/loan/{id}", 999L)) // Non-existent ID
                .andExpect(status().isNotFound()); // Expect 404 Not Found
    }
}