package com.loanrisk.controller;

import com.loanrisk.model.ScoringRule;
import com.loanrisk.repository.ScoringRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rules")
public class RuleController {

    private final ScoringRuleRepository scoringRuleRepository;

    @Autowired
    public RuleController(ScoringRuleRepository scoringRuleRepository) {
        this.scoringRuleRepository = scoringRuleRepository;
    }

    @GetMapping
    public List<ScoringRule> getActiveRules() {
        return scoringRuleRepository.findByEnabledTrueOrderByPriorityAsc();
    }
}