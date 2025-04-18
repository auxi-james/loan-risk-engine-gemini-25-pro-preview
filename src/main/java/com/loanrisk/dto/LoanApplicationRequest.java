package com.loanrisk.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanApplicationRequest {

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotNull(message = "Loan amount cannot be null")
    @Min(value = 1, message = "Loan amount must be positive")
    private Double loanAmount;

    @NotBlank(message = "Loan purpose cannot be blank")
    private String loanPurpose;

    @NotNull(message = "Requested term cannot be null")
    @Min(value = 1, message = "Requested term must be at least 1 month")
    private Integer requestedTermMonths;
}