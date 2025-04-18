package com.loanrisk.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ScoringRuleTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveScoringRule() {
        // Given
        ScoringRule rule = new ScoringRule(
                null, // ID is generated
                "Credit Score > 700",
                "creditScore",
                ">",
                "700",
                -10, // Points awarded/deducted
                1,   // Priority
                true // Enabled
        );

        // When
        ScoringRule savedRule = entityManager.persistFlushFind(rule);

        // Then
        assertThat(savedRule).isNotNull();
        assertThat(savedRule.getId()).isNotNull();
        assertThat(savedRule.getName()).isEqualTo("Credit Score > 700");
        assertThat(savedRule.getField()).isEqualTo("creditScore");
        assertThat(savedRule.getOperator()).isEqualTo(">");
        assertThat(savedRule.getValue()).isEqualTo("700");
        assertThat(savedRule.getRiskPoints()).isEqualTo(-10);
        assertThat(savedRule.getPriority()).isEqualTo(1);
        assertThat(savedRule.getEnabled()).isTrue();
    }
}