package com.loanrisk.service;

import com.loanrisk.dto.ScoringResult;
import com.loanrisk.model.Customer;
import com.loanrisk.model.LoanApplication;
import com.loanrisk.model.ScoringRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock
    private RuleLoadingService ruleLoadingService;

    @InjectMocks
    private ScoringServiceImpl scoringService; // Test the implementation

    private Customer testCustomer;
    private LoanApplication testApplication;

    @BeforeEach
    void setUp() {
        // Create common test data
        testCustomer = new Customer(1L, "Test Customer", 35, new BigDecimal("60000.00"),
                                    700, "Employed", new BigDecimal("5000.00"));
        testApplication = new LoanApplication(1L, testCustomer, new BigDecimal("10000.00"),
                                              "Car Purchase", 36, null, null, null, null, null);
    }

    private ScoringRule createRule(Long id, String name, String field, String operator, String value, int points, int priority, boolean enabled) {
        ScoringRule rule = new ScoringRule();
        rule.setId(id);
        rule.setName(name);
        rule.setField(field);
        rule.setOperator(operator);
        rule.setValue(value);
        rule.setRiskPoints(points);
        rule.setPriority(priority);
        rule.setEnabled(enabled); // Although service only gets active ones, good practice for clarity
        return rule;
    }

    @Test
    @DisplayName("Evaluate with No Active Rules")
    void evaluate_noActiveRules_returnsZeroScoreLowApprove() {
        when(ruleLoadingService.getActiveRules()).thenReturn(Collections.emptyList());

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(0, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertTrue(result.getExplanation().isEmpty());
    }

    @Test
    @DisplayName("Evaluate with One Matching Rule (Customer Age)")
    void evaluate_oneMatchingRuleAge_returnsCorrectScore() {
        ScoringRule ageRule = createRule(1L, "Age > 30", "age", ">", "30", 10, 1, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Collections.singletonList(ageRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(10, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Age > 30"), result.getExplanation());
    }

     @Test
    @DisplayName("Evaluate with One Non-Matching Rule (Customer Age)")
    void evaluate_oneNonMatchingRuleAge_returnsZeroScore() {
        ScoringRule ageRule = createRule(1L, "Age < 30", "age", "<", "30", 10, 1, true); // Customer age is 35
        when(ruleLoadingService.getActiveRules()).thenReturn(Collections.singletonList(ageRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(0, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertTrue(result.getExplanation().isEmpty());
    }

    @Test
    @DisplayName("Evaluate with Multiple Matching Rules (Customer and Application)")
    void evaluate_multipleMatchingRules_accumulatesScore() {
        ScoringRule ageRule = createRule(1L, "Age >= 35", "age", ">=", "35", 15, 1, true); // Matches (35)
        ScoringRule incomeRule = createRule(2L, "Income < 70k", "annualIncome", "<", "70000", 20, 2, true); // Matches (60k)
        ScoringRule loanAmountRule = createRule(3L, "Loan > 5k", "loanAmount", ">", "5000", 25, 3, true); // Matches (10k)
        ScoringRule nonMatchRule = createRule(4L, "Credit < 600", "creditScore", "<", "600", 50, 4, true); // No Match (700)

        when(ruleLoadingService.getActiveRules()).thenReturn(Arrays.asList(ageRule, incomeRule, loanAmountRule, nonMatchRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(15 + 20 + 25, result.getRiskScore()); // 60
        assertEquals("Medium", result.getRiskLevel());
        assertEquals("manual_review", result.getDecision());
        assertEquals(Arrays.asList("Age >= 35", "Income < 70k", "Loan > 5k"), result.getExplanation());
    }

    @Test
    @DisplayName("Evaluate with String Equality Rule (Employment Status)")
    void evaluate_stringEqualityRule_matchesCorrectly() {
        ScoringRule employmentRule = createRule(1L, "Employed Status", "employmentStatus", "==", "Employed", 5, 1, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Collections.singletonList(employmentRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(5, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Employed Status"), result.getExplanation());
    }

     @Test
    @DisplayName("Evaluate with String Inequality Rule (Employment Status)")
    void evaluate_stringInequalityRule_matchesCorrectly() {
        ScoringRule employmentRule = createRule(1L, "Not Unemployed", "employmentStatus", "!=", "Unemployed", 5, 1, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Collections.singletonList(employmentRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(5, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Not Unemployed"), result.getExplanation());
    }

    @Test
    @DisplayName("Evaluate with BigDecimal Comparison (Existing Debt)")
    void evaluate_bigDecimalComparison_matchesCorrectly() {
        ScoringRule debtRule = createRule(1L, "Debt <= 5000", "existingDebt", "<=", "5000.00", 12, 1, true); // Matches (5000.00)
        when(ruleLoadingService.getActiveRules()).thenReturn(Collections.singletonList(debtRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(12, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Debt <= 5000"), result.getExplanation());
    }

    @Test
    @DisplayName("Evaluate Score Boundary - Low to Medium (30)")
    void evaluate_scoreBoundaryLowToMedium_isLow() {
        ScoringRule rule1 = createRule(1L, "R1", "age", ">", "1", 15, 1, true);
        ScoringRule rule2 = createRule(2L, "R2", "creditScore", ">", "1", 15, 2, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Arrays.asList(rule1, rule2));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(30, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
    }

    @Test
    @DisplayName("Evaluate Score Boundary - Medium Start (31)")
    void evaluate_scoreBoundaryMediumStart_isMedium() {
        ScoringRule rule1 = createRule(1L, "R1", "age", ">", "1", 15, 1, true);
        ScoringRule rule2 = createRule(2L, "R2", "creditScore", ">", "1", 16, 2, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Arrays.asList(rule1, rule2));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(31, result.getRiskScore());
        assertEquals("Medium", result.getRiskLevel());
        assertEquals("manual_review", result.getDecision());
    }

    @Test
    @DisplayName("Evaluate Score Boundary - Medium to High (60)")
    void evaluate_scoreBoundaryMediumToHigh_isMedium() {
        ScoringRule rule1 = createRule(1L, "R1", "age", ">", "1", 30, 1, true);
        ScoringRule rule2 = createRule(2L, "R2", "creditScore", ">", "1", 30, 2, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Arrays.asList(rule1, rule2));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(60, result.getRiskScore());
        assertEquals("Medium", result.getRiskLevel());
        assertEquals("manual_review", result.getDecision());
    }

    @Test
    @DisplayName("Evaluate Score Boundary - High Start (61)")
    void evaluate_scoreBoundaryHighStart_isHigh() {
        ScoringRule rule1 = createRule(1L, "R1", "age", ">", "1", 30, 1, true);
        ScoringRule rule2 = createRule(2L, "R2", "creditScore", ">", "1", 31, 2, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Arrays.asList(rule1, rule2));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(61, result.getRiskScore());
        assertEquals("High", result.getRiskLevel());
        assertEquals("reject", result.getDecision());
    }

     @Test
    @DisplayName("Evaluate with Derived Field - Loan Ratio (Matches)")
    void evaluate_derivedFieldLoanRatio_matchesCorrectly() {
        // loanAmount (10000) / annualIncome (60000) = 0.1666...
        ScoringRule ratioRule = createRule(1L, "Loan Ratio < 0.2", "loanRatio", "<", "0.2", 22, 1, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Collections.singletonList(ratioRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(22, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Loan Ratio < 0.2"), result.getExplanation());
    }

     @Test
    @DisplayName("Evaluate with Derived Field - Existing Debt Ratio (Matches)")
    void evaluate_derivedFieldDebtRatio_matchesCorrectly() {
        // existingDebt (5000) / annualIncome (60000) = 0.0833...
        ScoringRule ratioRule = createRule(1L, "Debt Ratio < 0.1", "existingDebtRatio", "<", "0.1", 18, 1, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Collections.singletonList(ratioRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(18, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Debt Ratio < 0.1"), result.getExplanation());
    }

    @Test
    @DisplayName("Evaluate with Rule having Invalid Field Name")
    void evaluate_invalidFieldName_skipsRuleAndLogsWarning() {
        ScoringRule invalidFieldRule = createRule(1L, "Invalid Field", "nonExistentField", "==", "abc", 100, 1, true);
        ScoringRule validRule = createRule(2L, "Valid Rule", "age", ">", "30", 10, 2, true); // Should still run
        when(ruleLoadingService.getActiveRules()).thenReturn(Arrays.asList(invalidFieldRule, validRule));

        // We expect a log warning, but the evaluation should proceed
        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(10, result.getRiskScore()); // Only valid rule contributes
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Valid Rule"), result.getExplanation());
        // Verification of logging would require a logging framework test appender, omitted for brevity
    }

     @Test
    @DisplayName("Evaluate with Rule having Invalid Operator for Type")
    void evaluate_invalidOperatorForType_skipsRuleAndLogsWarning() {
        ScoringRule invalidOpRule = createRule(1L, "Invalid Op", "employmentStatus", ">", "abc", 100, 1, true); // Cannot use > for String
        ScoringRule validRule = createRule(2L, "Valid Rule", "age", ">", "30", 10, 2, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Arrays.asList(invalidOpRule, validRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(10, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Valid Rule"), result.getExplanation());
         // Verification of logging would require a logging framework test appender
    }

     @Test
    @DisplayName("Evaluate with Rule having Non-Numeric Value for Numeric Comparison")
    void evaluate_nonNumericRuleValueForNumericField_skipsRuleAndLogsError() {
        ScoringRule nonNumericValueRule = createRule(1L, "Non-Numeric Value", "age", "<", "abc", 100, 1, true); // 'abc' is not a number
        ScoringRule validRule = createRule(2L, "Valid Rule", "creditScore", ">", "600", 10, 2, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Arrays.asList(nonNumericValueRule, validRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(10, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Valid Rule"), result.getExplanation());
         // Verification of logging would require a logging framework test appender
    }

     @Test
    @DisplayName("Evaluate with Null Actual Value for Field")
    void evaluate_nullActualValue_skipsRuleAndLogsWarning() {
        // Set a field to null that a rule will target
        testCustomer.setEmploymentStatus(null);
        ScoringRule nullTargetRule = createRule(1L, "Null Target", "employmentStatus", "==", "Employed", 100, 1, true);
        ScoringRule validRule = createRule(2L, "Valid Rule", "age", ">", "30", 10, 2, true);
        when(ruleLoadingService.getActiveRules()).thenReturn(Arrays.asList(nullTargetRule, validRule));

        ScoringResult result = scoringService.evaluate(testApplication, testCustomer);

        assertEquals(10, result.getRiskScore());
        assertEquals("Low", result.getRiskLevel());
        assertEquals("approve", result.getDecision());
        assertEquals(Collections.singletonList("Valid Rule"), result.getExplanation());
         // Verification of logging would require a logging framework test appender
    }
}