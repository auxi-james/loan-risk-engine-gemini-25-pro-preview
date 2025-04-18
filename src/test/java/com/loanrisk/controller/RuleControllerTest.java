package com.loanrisk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanrisk.model.ScoringRule;
import com.loanrisk.repository.ScoringRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScoringRuleRepository scoringRuleRepository;

    private ScoringRule rule1_enabled_prio5;
    private ScoringRule rule2_enabled_prio10;
    private ScoringRule rule3_disabled_prio20;

    @BeforeEach
    void setUp() {
        // Clear existing data
        scoringRuleRepository.deleteAll();

        // Seed test data
        rule1_enabled_prio5 = new ScoringRule();
        rule1_enabled_prio5.setName("RuleA");
        rule1_enabled_prio5.setField("customer.age");
        rule1_enabled_prio5.setOperator(">");
        rule1_enabled_prio5.setValue("30");
        rule1_enabled_prio5.setRiskPoints(10); // Placeholder
        rule1_enabled_prio5.setPriority(5);
        rule1_enabled_prio5.setEnabled(true);

        rule2_enabled_prio10 = new ScoringRule();
        rule2_enabled_prio10.setName("RuleB");
        rule2_enabled_prio10.setField("customer.creditScore");
        rule2_enabled_prio10.setOperator("<");
        rule2_enabled_prio10.setValue("600");
        rule2_enabled_prio10.setRiskPoints(15); // Placeholder
        rule2_enabled_prio10.setPriority(10);
        rule2_enabled_prio10.setEnabled(true);

        rule3_disabled_prio20 = new ScoringRule();
        rule3_disabled_prio20.setName("RuleC");
        rule3_disabled_prio20.setField("loan.amount");
        rule3_disabled_prio20.setOperator(">");
        rule3_disabled_prio20.setValue("10000");
        rule3_disabled_prio20.setRiskPoints(20); // Placeholder
        rule3_disabled_prio20.setPriority(20);
        rule3_disabled_prio20.setEnabled(false);


        // Save rules and update local variables with persisted entities (including IDs)
        List<ScoringRule> savedRules = scoringRuleRepository.saveAll(Arrays.asList(rule1_enabled_prio5, rule2_enabled_prio10, rule3_disabled_prio20));
        rule1_enabled_prio5 = savedRules.stream().filter(r -> r.getName().equals("RuleA")).findFirst().orElseThrow();
        rule2_enabled_prio10 = savedRules.stream().filter(r -> r.getName().equals("RuleB")).findFirst().orElseThrow();
        rule3_disabled_prio20 = savedRules.stream().filter(r -> r.getName().equals("RuleC")).findFirst().orElseThrow();
    }

    @Test
    void testGetActiveRules() throws Exception {
        MvcResult result = mockMvc.perform(get("/rules")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // Expecting only the 2 enabled rules
                .andExpect(jsonPath("$[0].name").value(rule1_enabled_prio5.getName())) // Rule with priority 5 should be first
                .andExpect(jsonPath("$[0].priority").value(rule1_enabled_prio5.getPriority()))
                .andExpect(jsonPath("$[1].name").value(rule2_enabled_prio10.getName())) // Rule with priority 10 should be second
                .andExpect(jsonPath("$[1].priority").value(rule2_enabled_prio10.getPriority()))
                .andReturn();

        // Additionally verify the exact order and content using ObjectMapper
        String jsonResponse = result.getResponse().getContentAsString();
        List<ScoringRule> actualRules = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, ScoringRule.class));

        assertEquals(2, actualRules.size());
        assertEquals(rule1_enabled_prio5.getName(), actualRules.get(0).getName());
        assertEquals(rule1_enabled_prio5.getPriority(), actualRules.get(0).getPriority());
        assertEquals(rule2_enabled_prio10.getName(), actualRules.get(1).getName());
        assertEquals(rule2_enabled_prio10.getPriority(), actualRules.get(1).getPriority());

        // Ensure the disabled rule ID is not present
        long disabledRuleId = rule3_disabled_prio20.getId(); // Get ID after save
        boolean disabledRuleFound = actualRules.stream().anyMatch(rule -> rule.getId().equals(disabledRuleId));
        assertEquals(false, disabledRuleFound, "Disabled rule should not be present in the response");
    }
}