package com.loanrisk.service;

import com.loanrisk.model.ScoringRule;
import java.util.List;

/**
 * Service interface for loading scoring rules.
 */
public interface RuleLoadingService {

    /**
     * Retrieves all active scoring rules, ordered by priority.
     *
     * @return A list of active ScoringRule objects.
     */
    List<ScoringRule> getActiveRules();
}