package com.loanrisk.service;

import com.loanrisk.model.ScoringRule;
import com.loanrisk.repository.ScoringRuleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Implementation of the RuleLoadingService.
 */
@Service
public class RuleLoadingServiceImpl implements RuleLoadingService {

    private final ScoringRuleRepository scoringRuleRepository;

    // Constructor injection for the repository
    public RuleLoadingServiceImpl(ScoringRuleRepository scoringRuleRepository) {
        this.scoringRuleRepository = scoringRuleRepository;
    }

    /**
     * Retrieves all active scoring rules from the repository, ordered by priority.
     *
     * @return A list of active ScoringRule objects.
     */
    @Override
    public List<ScoringRule> getActiveRules() {
        // Delegate the call to the repository method
        return scoringRuleRepository.findByEnabledTrueOrderByPriorityAsc();
    }
}