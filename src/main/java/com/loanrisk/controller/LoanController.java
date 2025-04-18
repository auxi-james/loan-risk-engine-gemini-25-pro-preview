package com.loanrisk.controller;

import com.loanrisk.dto.LoanApplicationRequest;
import com.loanrisk.dto.LoanApplicationResponse;
import com.loanrisk.dto.ScoringResult;
import com.loanrisk.model.Customer;
import com.loanrisk.model.LoanApplication;
import com.loanrisk.repository.CustomerRepository;
import com.loanrisk.repository.LoanApplicationRepository;
import com.loanrisk.service.ScoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal; // Add import for BigDecimal
import java.time.LocalDateTime;
import java.util.List; // Add import for List

@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
public class LoanController {

    private final ScoringService scoringService;
    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> applyForLoan(@Valid @RequestBody LoanApplicationRequest request) {
        // Fetch Customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with ID: " + request.getCustomerId()));

        // Create Entity
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setCustomer(customer);
        // Convert Double to BigDecimal
        loanApplication.setLoanAmount(BigDecimal.valueOf(request.getLoanAmount()));
        loanApplication.setLoanPurpose(request.getLoanPurpose());
        loanApplication.setRequestedTermMonths(request.getRequestedTermMonths());
        // createdAt is handled by @CreationTimestamp, no need to set applicationDate or initial status

        // Evaluate
        ScoringResult scoringResult = scoringService.evaluate(loanApplication, customer);

        // Update Entity
        loanApplication.setRiskScore(scoringResult.getRiskScore());
        loanApplication.setRiskLevel(scoringResult.getRiskLevel());
        loanApplication.setDecision(scoringResult.getDecision());
        loanApplication.setExplanation(scoringResult.getExplanation()); // Set the List<String> directly
        // No status field, decision field holds the outcome

        // Save
        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);

        // Return Response
        LoanApplicationResponse response = mapToResponse(savedApplication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // Use 201 Created for new resource
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> getLoanApplication(@PathVariable Long id) {
        // Fetch Application
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan application not found with ID: " + id));

        // Map and Return
        LoanApplicationResponse response = mapToResponse(loanApplication);
        return ResponseEntity.ok(response);
    }

    // Helper method to map entity to response DTO
    // Helper method to map entity to response DTO
    private LoanApplicationResponse mapToResponse(LoanApplication application) {
        LoanApplicationResponse response = new LoanApplicationResponse();
        response.setLoanId(application.getId());
        response.setRiskScore(application.getRiskScore());
        response.setRiskLevel(application.getRiskLevel());
        response.setDecision(application.getDecision());
        // Directly use the explanation list from the entity
        response.setExplanation(application.getExplanation() != null ? application.getExplanation() : List.of());
        return response;
    }
}