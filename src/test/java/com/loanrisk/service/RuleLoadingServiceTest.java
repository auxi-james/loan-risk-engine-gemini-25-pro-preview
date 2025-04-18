package com.loanrisk.service;

import com.loanrisk.model.ScoringRule;
import com.loanrisk.repository.ScoringRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the RuleLoadingServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class RuleLoadingServiceTest {

    @Mock
    private ScoringRuleRepository scoringRuleRepository;

    @InjectMocks
    private RuleLoadingServiceImpl ruleLoadingService;

    @Test
    void getActiveRules_shouldReturnActiveRulesOrderedByPriority() {
        // Arrange
        // Using the @AllArgsConstructor: id, name, field, operator, value, riskPoints, priority, enabled
        ScoringRule rule1 = new ScoringRule(null, "CREDIT_SCORE_LOW", "customer.creditScore", "<", "600", 50, 10, true);
        ScoringRule rule2 = new ScoringRule(null, "DEBT_TO_INCOME_HIGH", "loan.debtToIncomeRatio", ">", "0.5", 30, 20, true);
        List<ScoringRule> sampleRules = Arrays.asList(rule1, rule2);

        // Mock the repository call
        when(scoringRuleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(sampleRules);

        // Act
        List<ScoringRule> activeRules = ruleLoadingService.getActiveRules();

        // Assert
        // Verify the repository method was called exactly once
        verify(scoringRuleRepository, times(1)).findByEnabledTrueOrderByPriorityAsc();
        // Verify the returned list is the same as the mocked list
        assertEquals(sampleRules, activeRules, "The returned list of active rules should match the sample rules.");
    }
}