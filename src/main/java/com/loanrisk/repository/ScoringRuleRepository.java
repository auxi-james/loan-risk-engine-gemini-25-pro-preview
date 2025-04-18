package com.loanrisk.repository;

import com.loanrisk.model.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {
    List<ScoringRule> findByEnabledTrueOrderByPriorityAsc();
}