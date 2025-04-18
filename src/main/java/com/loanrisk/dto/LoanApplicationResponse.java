package com.loanrisk.dto;

import lombok.Data;
import java.util.List;

@Data
public class LoanApplicationResponse {

    private Long loanId;
    private Integer riskScore;
    private String riskLevel;
    private String decision;
    private List<String> explanation;
}