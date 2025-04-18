package com.loanrisk.service;

import com.loanrisk.dto.ScoringResult;
import com.loanrisk.model.Customer;
import com.loanrisk.model.LoanApplication;
import com.loanrisk.model.ScoringRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j // Add logging
public class ScoringServiceImpl implements ScoringService {

    private final RuleLoadingService ruleLoadingService;
    private static final int BASE_SCORE = 500; // Define a base score

    @Override
    public ScoringResult evaluate(LoanApplication application, Customer customer) {
        List<ScoringRule> activeRules = ruleLoadingService.getActiveRules();
        int riskScore = BASE_SCORE; // Initialize with base score
        List<String> explanation = new ArrayList<>();

        log.info("Starting scoring evaluation for application ID: {} and customer ID: {}. Base score: {}", application.getId(), customer.getId(), BASE_SCORE);
        log.debug("Found {} active rules.", activeRules.size());

        for (ScoringRule rule : activeRules) {
            log.debug("Evaluating rule: {}", rule.getName());
            try {
                Object actualValue = getValueFromField(customer, application, rule.getField());
                String ruleValue = rule.getValue();
                String operator = rule.getOperator();

                if (actualValue == null) {
                    log.warn("Actual value for field '{}' is null for rule '{}'. Skipping comparison.", rule.getField(), rule.getName());
                    continue; // Skip rule if field value is null
                }

                log.debug("Rule '{}': Field='{}', Operator='{}', RuleValue='{}', ActualValue='{}'",
                          rule.getName(), rule.getField(), operator, ruleValue, actualValue);

                if (compareValues(actualValue, operator, ruleValue)) {
                    riskScore += rule.getRiskPoints();
                    explanation.add(rule.getName());
                    log.debug("Rule '{}' matched. Added {} points. Current score: {}", rule.getName(), rule.getRiskPoints(), riskScore);
                }
            } catch (Exception e) {
                // Log error and continue with the next rule
                log.error("Error evaluating rule '{}' (ID: {}): {}. Field: '{}', Operator: '{}', Value: '{}'. Skipping rule.",
                          rule.getName(), rule.getId(), e.getMessage(), rule.getField(), rule.getOperator(), rule.getValue(), e);
            }
        }

        String riskLevel = determineRiskLevel(riskScore);
        String decision = determineDecision(riskLevel);

        log.info("Scoring evaluation completed for application ID: {}. Score: {}, Level: {}, Decision: {}",
                 application.getId(), riskScore, riskLevel, decision);

        return new ScoringResult(riskScore, riskLevel, decision, explanation);
    }

    /**
     * Extracts the value of a specified field from either the Customer or LoanApplication object.
     * Handles direct fields and potential derived fields.
     */
    private Object getValueFromField(Customer customer, LoanApplication application, String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            log.warn("Field name is null or empty.");
            return null;
        }

        // Customer fields
        if ("age".equalsIgnoreCase(fieldName)) return customer.getAge();
        if ("annualIncome".equalsIgnoreCase(fieldName)) return customer.getAnnualIncome();
        if ("creditScore".equalsIgnoreCase(fieldName)) return customer.getCreditScore();
        if ("employmentStatus".equalsIgnoreCase(fieldName)) return customer.getEmploymentStatus();
        if ("existingDebt".equalsIgnoreCase(fieldName)) return customer.getExistingDebt();
        if ("name".equalsIgnoreCase(fieldName)) return customer.getName(); // Less likely for rules, but possible

        // LoanApplication fields
        if ("loanAmount".equalsIgnoreCase(fieldName)) return application.getLoanAmount();
        if ("loanPurpose".equalsIgnoreCase(fieldName)) return application.getLoanPurpose();
        if ("requestedTermMonths".equalsIgnoreCase(fieldName)) return application.getRequestedTermMonths();

        // Derived fields (Example - implement calculation if needed)
        if ("loanRatio".equalsIgnoreCase(fieldName)) {
            // Example: loanAmount / annualIncome
            if (customer.getAnnualIncome() != null && customer.getAnnualIncome().compareTo(BigDecimal.ZERO) > 0 && application.getLoanAmount() != null) {
                return application.getLoanAmount().divide(customer.getAnnualIncome(), 4, RoundingMode.HALF_UP); // Use scale and rounding
            } else {
                log.warn("Cannot calculate derived field '{}': annualIncome or loanAmount is null or zero.", fieldName);
                return null; // Or handle as appropriate (e.g., return BigDecimal.ZERO)
            }
        }
        if ("existingDebtRatio".equalsIgnoreCase(fieldName)) {
            // Example: existingDebt / annualIncome
             if (customer.getAnnualIncome() != null && customer.getAnnualIncome().compareTo(BigDecimal.ZERO) > 0 && customer.getExistingDebt() != null) {
                return customer.getExistingDebt().divide(customer.getAnnualIncome(), 4, RoundingMode.HALF_UP); // Use scale and rounding
            } else {
                log.warn("Cannot calculate derived field '{}': annualIncome or existingDebt is null or zero.", fieldName);
                return null; // Or handle as appropriate
            }
        }


        log.warn("Field '{}' not found in Customer or LoanApplication or derived fields.", fieldName);
        return null; // Field not found
    }

    /**
     * Compares the actual value from the object with the rule's value string using the specified operator.
     * Handles type conversions and different operators.
     */
    private boolean compareValues(Object actualValue, String operator, String ruleValueStr) {
        if (actualValue == null || ruleValueStr == null || operator == null) {
            log.warn("Cannot compare values: one or more inputs are null (actualValue={}, operator={}, ruleValueStr={})", actualValue, operator, ruleValueStr);
            return false;
        }

        try {
            // Handle String comparison first
            if (actualValue instanceof String) {
                String actualStr = (String) actualValue;
                // Currently only supporting equality for strings
                if ("==".equals(operator)) {
                    return actualStr.equalsIgnoreCase(ruleValueStr.trim()); // Case-insensitive comparison for strings
                } else if ("!=".equals(operator)) {
                     return !actualStr.equalsIgnoreCase(ruleValueStr.trim());
                } else {
                    log.warn("Unsupported operator '{}' for String comparison. RuleValue: '{}'", operator, ruleValueStr);
                    return false;
                }
            }

            // Handle Numeric comparison (Integer, BigDecimal)
            if (actualValue instanceof Number) {
                BigDecimal actualNum = BigDecimal.ZERO; // Initialize
                 if (actualValue instanceof BigDecimal) {
                    actualNum = (BigDecimal) actualValue;
                } else if (actualValue instanceof Integer) {
                    actualNum = new BigDecimal((Integer) actualValue);
                } else {
                     log.warn("Unsupported Number type: {}. RuleValue: '{}'", actualValue.getClass().getName(), ruleValueStr);
                     return false; // Or handle other numeric types if necessary
                }


                BigDecimal ruleNum = new BigDecimal(ruleValueStr.trim());
                int comparisonResult = actualNum.compareTo(ruleNum);

                switch (operator) {
                    case "<":  return comparisonResult < 0;
                    case ">":  return comparisonResult > 0;
                    case "==": return comparisonResult == 0;
                    case "<=": return comparisonResult <= 0;
                    case ">=": return comparisonResult >= 0;
                    case "!=": return comparisonResult != 0; // Added not equals
                    default:
                        log.warn("Unsupported numeric operator: '{}'. RuleValue: '{}'", operator, ruleValueStr);
                        return false;
                }
            }

            log.warn("Unsupported type for comparison: {}. RuleValue: '{}'", actualValue.getClass().getName(), ruleValueStr);
            return false;

        } catch (NumberFormatException e) {
            log.error("Error parsing rule value '{}' as a number for comparison against actual value '{}' ({}). Operator: '{}'",
                      ruleValueStr, actualValue, actualValue.getClass().getSimpleName(), operator, e);
            return false;
        } catch (Exception e) {
             log.error("Unexpected error during comparison. ActualValue: '{}' ({}), Operator: '{}', RuleValue: '{}'",
                       actualValue, actualValue.getClass().getSimpleName(), operator, ruleValueStr, e);
            return false;
        }
    }

    // Adjusted thresholds based on BASE_SCORE = 500
    private String determineRiskLevel(int riskScore) {
        if (riskScore < 450) return "High";    // Example: Score below 450 is High risk
        if (riskScore < 650) return "Medium";  // Example: Score between 450 and 649 is Medium risk
        return "Low";                          // Example: Score 650 and above is Low risk
    }

    // Decision logic might need adjustment based on risk levels
    private String determineDecision(String riskLevel) {
        switch (riskLevel) {
            case "Low":    return "APPROVED"; // Consistent casing with tests
            case "Medium": return "MANUAL_REVIEW"; // Consistent casing
            case "High":   return "DECLINED"; // Consistent casing (or REJECTED)
            default:
                log.error("Unknown risk level encountered: {}", riskLevel);
                return "ERROR"; // Should not happen
        }
    }
}