package com.loanrisk.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing the result of a scoring evaluation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringResult {

    private int riskScore;
    private String riskLevel;
    private String decision;
    private List<String> explanation;

}