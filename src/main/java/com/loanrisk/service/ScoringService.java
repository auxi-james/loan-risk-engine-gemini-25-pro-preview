package com.loanrisk.service;

import com.loanrisk.dto.ScoringResult;
import com.loanrisk.model.Customer;
import com.loanrisk.model.LoanApplication;

/**
 * Service interface for evaluating loan applications based on scoring rules.
 */
public interface ScoringService {

    /**
     * Evaluates a loan application against active scoring rules.
     *
     * @param application The loan application details.
     * @param customer    The customer details.
     * @return A ScoringResult containing the risk score, level, decision, and explanation.
     */
    ScoringResult evaluate(LoanApplication application, Customer customer);

}