# Loan Risk Scoring Engine - Implementation Plan

This document outlines the step-by-step plan for implementing the Loan Risk Scoring Engine microservice. Each step includes implementation, testing, fixing issues, and committing the changes as required.

**Core Technologies:** Java, Spring Boot (WebMVC, DataJPA), H2 (for testing), Postgres (optional for deployment), Maven.

---

## Phase 1: Project Foundation

### Step 1: Setup Project & Dependencies

*   **Implement:**
    *   Ensure the base Spring Boot project structure is correct.
    *   Add necessary dependencies to `pom.xml`:
        *   `spring-boot-starter-web`
        *   `spring-boot-starter-data-jpa`
        *   `com.h2database:h2` (runtime scope)
        *   `org.postgresql:postgresql` (optional, runtime scope if using Postgres)
        *   `org.projectlombok:lombok` (optional, provided scope)
        *   `spring-boot-starter-validation` (optional, for request validation)
        *   `spring-boot-starter-test` (test scope)
    *   Configure `src/main/resources/application.properties` for the H2 database (e.g., `spring.datasource.url=jdbc:h2:mem:testdb`, `spring.datasource.driverClassName=org.h2.Driver`, `spring.datasource.username=sa`, `spring.datasource.password=password`, `spring.jpa.database-platform=org.hibernate.dialect.H2Dialect`, `spring.jpa.hibernate.ddl-auto=update`).
*   **Test:**
    *   Write a basic application context loading test (`LoanRiskApplicationTests.java`) to ensure the Spring context starts correctly with the added dependencies.
    *   Run tests: `./mvnw test`
*   **Fix:** Address any dependency issues or configuration errors until tests pass.
*   **Commit:** `git commit -m "feat: Initial project setup and dependencies"`

### Step 2: Define Core Data Entities

*   **Implement:**
    *   Create JPA entity classes in `src/main/java/com/loanrisk/model/` (or similar package):
        *   `Customer.java` (with fields: id, name, age, annualIncome, creditScore, employmentStatus, existingDebt) - Use `@Entity`, `@Id`, `@GeneratedValue`, potentially `@Enumerated` for status.
        *   `ScoringRule.java` (with fields: id, name, field, operator, value, riskPoints, priority, enabled) - Use `@Entity`, `@Id`, `@GeneratedValue`. Note `value` might need flexible typing (e.g., String, handle conversion later).
        *   `LoanApplication.java` (with fields: id, customerId (as `@ManyToOne Customer`), loanAmount, loanPurpose, requestedTermMonths, riskScore, riskLevel, decision, explanation (`@ElementCollection` or similar for list of strings), createdAt (`@CreationTimestamp`)). Use `@Entity`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@CreationTimestamp`.
*   **Test:**
    *   Write basic JPA entity tests (e.g., using `@DataJpaTest`) to verify that each entity can be persisted and retrieved correctly via an `EntityManager` or temporary repository.
    *   Run tests: `./mvnw test`
*   **Fix:** Correct any mapping errors, annotation issues, or constraint violations until tests pass.
*   **Commit:** `git commit -m "feat: Define core JPA entities (Customer, LoanApplication, ScoringRule)"`

### Step 3: Create JPA Repositories

*   **Implement:**
    *   Create Spring Data JPA repository interfaces in `src/main/java/com/loanrisk/repository/`:
        *   `CustomerRepository extends JpaRepository<Customer, Long>`
        *   `LoanApplicationRepository extends JpaRepository<LoanApplication, Long>`
        *   `ScoringRuleRepository extends JpaRepository<ScoringRule, Long>`
    *   Add a custom query method to `ScoringRuleRepository`: `List<ScoringRule> findByEnabledTrueOrderByPriorityAsc();`
*   **Test:**
    *   Write tests for the repositories (`@DataJpaTest`). Verify basic CRUD operations work and test the custom `findByEnabledTrueOrderByPriorityAsc` method.
    *   Run tests: `./mvnw test`
*   **Fix:** Address any issues with repository definitions or query methods until tests pass.
*   **Commit:** `git commit -m "feat: Create JPA repositories"`

---

## Phase 2: API Implementation & Core Logic

### Step 4: Implement Customer API

*   **Implement:**
    *   Create `CustomerService.java` (interface and implementation) for business logic separation.
    *   Create `CustomerController.java` in `src/main/java/com/loanrisk/controller/`.
    *   Inject `CustomerService` (or `CustomerRepository`).
    *   Implement `POST /customers`: Takes customer data (consider a DTO), validates, saves via service/repository, returns created customer (or ID/status).
    *   Implement `GET /customers/{id}`: Takes ID, fetches via service/repository, handles not found cases, returns customer data.
*   **Test:**
    *   Write integration tests (`@SpringBootTest`, `@AutoConfigureMockMvc`) for the `CustomerController`. Test creating a customer, retrieving it, and handling invalid IDs/requests.
    *   Run tests: `./mvnw test`
*   **Fix:** Debug controller logic, service interactions, request/response mapping, and validation until tests pass.
*   **Commit:** `git commit -m "feat: Implement Customer API endpoints (/customers)"`

### Step 5: Implement Scoring Rule API

*   **Implement:**
    *   Create `RuleService.java` (optional, could use repository directly in controller for this simple case).
    *   Create `RuleController.java`.
    *   Inject `ScoringRuleRepository` (or `RuleService`).
    *   Implement `GET /rules`: Fetches enabled rules using `scoringRuleRepository.findByEnabledTrueOrderByPriorityAsc()`, returns the list.
*   **Test:**
    *   Write integration tests for the `RuleController`. Seed some test rules (enabled and disabled) and verify that only enabled rules are returned in the correct order.
    *   Run tests: `./mvnw test`
*   **Fix:** Correct repository usage or response mapping until tests pass.
*   **Commit:** `git commit -m "feat: Implement Scoring Rule API endpoint (/rules)"`

### Step 6: Implement Rule Loading Service

*   **Implement:**
    *   Create `RuleLoadingService.java`.
    *   Inject `ScoringRuleRepository`.
    *   Implement a method `List<ScoringRule> getActiveRules()` that calls `scoringRuleRepository.findByEnabledTrueOrderByPriorityAsc()`.
    *   (Optional: Add caching later if needed, but not in this initial step).
*   **Test:**
    *   Write unit tests (`@ExtendWith(MockitoExtension.class)`) for `RuleLoadingService`. Mock the `ScoringRuleRepository` and verify the correct method is called and the results are returned.
    *   Run tests: `./mvnw test`
*   **Fix:** Ensure correct interaction with the repository mock until tests pass.
*   **Commit:** `git commit -m "feat: Implement service for loading active scoring rules"`

### Step 7: Implement Core Scoring Logic Service

*   **Implement:**
    *   Create `ScoringService.java`.
    *   Inject `RuleLoadingService` (or `ScoringRuleRepository`).
    *   Define input DTOs or use entities directly (e.g., `LoanApplication`, `Customer`).
    *   Define a response DTO `ScoringResult` containing `riskScore`, `riskLevel`, `decision`, `explanation` (List<String>).
    *   Implement the core `ScoringResult evaluate(LoanApplication application, Customer customer)` method:
        *   Get active rules via `RuleLoadingService`.
        *   Initialize `riskScore = 0`, `explanation = new ArrayList<>()`.
        *   Iterate through rules by priority:
            *   Get rule `field`, `operator`, `value`, `riskPoints`.
            *   Extract the corresponding value from `customer` or `application` data (handle potential derived fields like `loanAmount / annualIncome` if required by rules, though keep simple initially). Use reflection or a switch/map based on `rule.getField()`.
            *   Compare the extracted value with `rule.getValue()` using the `rule.getOperator()` (handle different operators `<`, `>`, `==`, etc., and data types).
            *   If the rule condition is met, add `rule.getRiskPoints()` to `riskScore` and add `rule.getName()` to `explanation`.
        *   Determine `riskLevel` and `decision` based on the final `riskScore` using the ranges from `requirements.md`.
        *   Return the `ScoringResult`.
*   **Test:**
    *   Write comprehensive unit tests for `ScoringService`.
        *   Mock `RuleLoadingService` to provide controlled sets of rules.
        *   Test various scenarios: no rules, rules matching/not matching, different operators, different data types, score boundary conditions for levels/decisions, derived fields (if implemented).
    *   Run tests: `./mvnw test`
*   **Fix:** Debug the rule application logic, data extraction, comparisons, score calculation, and level/decision mapping until all unit tests pass.
*   **Commit:** `git commit -m "feat: Implement core scoring engine logic"`

### Step 8: Implement Loan Application API

*   **Implement:**
    *   Create `LoanController.java`.
    *   Create DTOs: `LoanApplicationRequest` (customerId, loanAmount, etc.) and `LoanApplicationResponse` (loanId, riskScore, riskLevel, decision, explanation).
    *   Inject `ScoringService`, `CustomerRepository`, `LoanApplicationRepository`.
    *   Implement `POST /loan/apply`:
        *   Receive `LoanApplicationRequest`.
        *   Validate request data.
        *   Fetch the `Customer` using `customerId` (handle not found).
        *   Create a `LoanApplication` entity from the request, associating the fetched `Customer`.
        *   Call `scoringService.evaluate(loanApplication, customer)`.
        *   Update the `LoanApplication` entity with the results from `ScoringResult`.
        *   Save the updated `LoanApplication` using `loanApplicationRepository.save()`.
        *   Return `LoanApplicationResponse` mapping the saved application ID and scoring results.
    *   Implement `GET /loan/{id}`:
        *   Fetch `LoanApplication` by ID using repository (handle not found).
        *   Map entity to `LoanApplicationResponse` (or a more detailed DTO if needed).
        *   Return the response.
*   **Test:**
    *   Write integration tests (`@SpringBootTest`, `@AutoConfigureMockMvc`) for `LoanController`.
        *   Seed necessary test data (customer, rules) using `@Sql` or test setup methods.
        *   Test `POST /loan/apply` with valid and invalid requests, verifying the saved `LoanApplication` has correct scoring results and the response is accurate.
        *   Test `GET /loan/{id}` for existing and non-existing applications.
    *   Run tests: `./mvnw test`
*   **Fix:** Debug controller logic, service integration, DTO mapping, data persistence, and error handling until tests pass.
*   **Commit:** `git commit -m "feat: Implement Loan Application API endpoints"`

---

## Phase 3: Finalization

### Step 9: Database Seeding (Optional but Recommended)

*   **Implement:**
    *   Create `src/main/resources/data.sql` (this runs automatically with H2 when `ddl-auto` is not `none`).
    *   Add sample `INSERT` statements for `ScoringRule` based on examples in `requirements.md`. Ensure `enabled` is true for rules to be active.
    *   Add sample `Customer` data if helpful for manual testing or demos.
*   **Test:**
    *   Run the application (`./mvnw spring-boot:run`).
    *   Manually test API endpoints (e.g., using `curl` or Postman) or verify via `GET /rules` and `GET /customers/:id` that seeded data is present.
    *   Ensure existing automated tests still pass: `./mvnw test` (adjust tests if seeding conflicts, e.g., by cleaning DB before tests or using specific test profiles).
*   **Fix:** Correct SQL syntax or adjust tests as needed.
*   **Commit:** `git commit -m "chore: Add initial database seeding for rules and customers"`

### Step 10: Create README.md

*   **Implement:**
    *   Create `README.md` in the project root.
    *   Add:
        *   Project title and brief description.
        *   Instructions on how to build and run (`./mvnw clean package`, `./mvnw spring-boot:run`).
        *   Summary of available API endpoints (`POST /customers`, `GET /customers/{id}`, `POST /loan/apply`, `GET /loan/{id}`, `GET /rules`).
        *   Mention database configuration (H2 default, Postgres option).
*   **Test:** (Manual Review) Read the README for clarity and accuracy.
*   **Fix:** Update content as needed.
*   **Commit:** `git commit -m "docs: Add project README.md"`

### Step 11: Final Review & Polish (Optional)

*   **Implement:**
    *   Review code for consistency, comments, potential bugs, error handling improvements, and logging.
    *   Ensure all functional requirements from `doc/requirements.md` are addressed.
*   **Test:**
    *   Run all tests one final time: `./mvnw test`
*   **Fix:** Address any remaining issues found during review or testing.
*   **Commit:** `git commit -m "chore: Final review and polish"`

---