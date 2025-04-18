package com.loanrisk.repository;

import com.loanrisk.model.ScoringRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ScoringRuleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScoringRuleRepository scoringRuleRepository;

    @BeforeEach
    public void cleanupDatabase() {
        scoringRuleRepository.deleteAll();
        entityManager.flush(); // Ensure deletes are executed before the next test
    }

    @Test
    public void whenSaveRule_thenFindById() {
        ScoringRule rule = new ScoringRule();
        // Using setters as @NoArgsConstructor is present
        rule.setName("Credit Score > 700"); // Corrected field name
        rule.setField("creditScore"); // Example field
        rule.setOperator(">"); // Example operator
        rule.setValue("700"); // Example value
        rule.setRiskPoints(0); // Example points
        rule.setPriority(10);
        rule.setEnabled(true); // Corrected setter for Boolean

        ScoringRule savedRule = scoringRuleRepository.save(rule);
        entityManager.flush();

        Optional<ScoringRule> foundRuleOpt = scoringRuleRepository.findById(savedRule.getId());

        assertThat(foundRuleOpt).isPresent();
        assertThat(foundRuleOpt.get().getName()).isEqualTo(rule.getName()); // Corrected field name
        assertThat(foundRuleOpt.get().getEnabled()).isTrue(); // Corrected getter for Boolean
    }

    @Test
    public void whenFindAll_thenReturnRuleList() {
        // Using @AllArgsConstructor: (id, name, field, operator, value, riskPoints, priority, enabled)
        ScoringRule rule1 = new ScoringRule(null, "Income > 50k", "annualIncome", ">", "50000", 0, 20, true);
        ScoringRule rule2 = new ScoringRule(null, "Debt Ratio < 0.4", "debtRatio", "<", "0.4", 0, 5, true); // Assuming debtRatio field exists or is calculated elsewhere
        entityManager.persist(rule1);
        entityManager.persist(rule2);
        entityManager.flush();

        List<ScoringRule> rules = scoringRuleRepository.findAll();

        assertThat(rules).hasSize(2).extracting(ScoringRule::getName) // Corrected field name
                .containsExactlyInAnyOrder("Income > 50k", "Debt Ratio < 0.4");
    }

    @Test
    public void whenDeleteRule_thenNotFound() {
        ScoringRule rule = new ScoringRule(null, "Age < 18", "age", "<", "18", -100, 1, true); // Example data
        ScoringRule savedRule = entityManager.persistFlushFind(rule);

        scoringRuleRepository.deleteById(savedRule.getId());
        entityManager.flush();

        Optional<ScoringRule> foundRuleOpt = scoringRuleRepository.findById(savedRule.getId());

        assertThat(foundRuleOpt).isNotPresent();
    }

    @Test
    public void whenFindByEnabledTrueOrderByPriorityAsc_thenReturnCorrectRules() {
        // Using @AllArgsConstructor: (id, name, field, operator, value, riskPoints, priority, enabled)
        // Rule 1: Enabled, Priority 10
        ScoringRule rule1 = new ScoringRule(null, "Enabled Rule Prio 10", "field1", "op1", "val1", 10, 10, true);
        entityManager.persist(rule1);

        // Rule 2: Disabled, Priority 5
        ScoringRule rule2 = new ScoringRule(null, "Disabled Rule Prio 5", "field2", "op2", "val2", 5, 5, false);
        entityManager.persist(rule2);

        // Rule 3: Enabled, Priority 20
        ScoringRule rule3 = new ScoringRule(null, "Enabled Rule Prio 20", "field3", "op3", "val3", 20, 20, true);
        entityManager.persist(rule3);

        // Rule 4: Enabled, Priority 5 (Same as disabled rule 2, but enabled)
        ScoringRule rule4 = new ScoringRule(null, "Enabled Rule Prio 5", "field4", "op4", "val4", 5, 5, true);
        entityManager.persist(rule4);

        entityManager.flush();

        List<ScoringRule> enabledRules = scoringRuleRepository.findByEnabledTrueOrderByPriorityAsc();

        // Should find 3 enabled rules (rule1, rule3, rule4)
        assertThat(enabledRules).hasSize(3);
        // Should be ordered by priority: rule4 (5), rule1 (10), rule3 (20)
        assertThat(enabledRules).extracting(ScoringRule::getPriority)
                .containsExactly(5, 10, 20);
        assertThat(enabledRules).extracting(ScoringRule::getName) // Corrected field name
                .containsExactly("Enabled Rule Prio 5", "Enabled Rule Prio 10", "Enabled Rule Prio 20");
        // Ensure only enabled rules are returned
        assertThat(enabledRules).allMatch(ScoringRule::getEnabled); // Corrected getter for Boolean
    }
}