# Loan Risk Scoring Engine

---

## 🎯 Objective

Build a **microservice** that evaluates loan applications by calculating a **risk score** using a dynamic, database-driven rule engine.
All relevant data — including **customers**, **loan applications**, and **scoring rules** — must be persisted in a relational database.

---

## 🛠 Requirements

### ✅ Functional Requirements

- Store and manage **customer profiles**
- Store and evaluate **loan applications**
- Store all **scoring rules** in the database
  - **All rules must be loaded from the DB — no hardcoded logic**
  - Apply scoring rules dynamically during evaluation
- Return a **risk score**, **risk level**, and **approval decision**
- Persist all evaluation results

---

## 📦 API Specification

### `POST /customers`
Create a new customer profile.

### `GET /customers/:id`
Fetch customer details.

---

### `POST /loan/apply`
Submit a loan application for evaluation.

**Request Body:**
```json
{
  "customerId": "abc123",
  "loanAmount": 15000,
  "loanPurpose": "car",
  "requestedTermMonths": 24
}
```

Returns:
- `loanId`
- `riskScore`
- `riskLevel`
- `decision`
- `explanation` (list of triggered rules)

---

### `GET /loan/:id`
Fetch loan application and its evaluation result.

---

### `GET /rules`
Retrieve all active scoring rules.

---

## 🧾 Required Data Model

### 📁 Customer

| Field              | Type     |
|--------------------|----------|
| `id`               | number     |
| `name`             | string   |
| `age`              | integer  |
| `annualIncome`     | float    |
| `creditScore`      | integer  |
| `employmentStatus` | enum     |
| `existingDebt`     | float    |

---

### 📁 LoanApplication

| Field                | Type     |
|----------------------|----------|
| `id`                 | number     |
| `customerId`         | FK       |
| `loanAmount`         | float    |
| `loanPurpose`        | string   |
| `requestedTermMonths`| integer  |
| `riskScore`          | integer  |
| `riskLevel`          | string   |
| `decision`           | string   |
| `explanation`        | text[]   |
| `createdAt`          | timestamp |

---

### 📁 ScoringRule (**Required**)

| Field       | Type     |
|-------------|----------|
| `id`        | number     |
| `name`      | string   |
| `field`     | string   |
| `operator`  | string   |
| `value`     | number or string |
| `riskPoints`| integer  |
| `priority`  | integer  |
| `enabled`   | boolean  |

---

## 🧠 Example Scoring Rules (Stored in DB)

| Rule Name           | Field         | Operator | Value     | Points |
|---------------------|---------------|----------|-----------|--------|
| Credit too low      | creditScore   | `<`      | 600       | +30    |
| Credit average      | creditScore   | `<`      | 700       | +15    |
| Loan-to-income high | loanRatio     | `>`      | 0.5       | +25    |
| Debt is high        | existingDebtRatio | `>`  | 0.4       | +20    |
| Too young           | age           | `<`      | 21        | +20    |
| Vacation loan       | loanPurpose   | `==`     | vacation  | +10    |

> Teams may derive computed fields (e.g., loanRatio) in their rule engine.

---

## 🧮 Risk Score to Decision

| Score Range | Risk Level | Decision        |
|-------------|------------|-----------------|
| 0–30        | Low        | approve         |
| 31–60       | Medium     | manual_review   |
| 61–100      | High       | reject          |

---

## 💾 Technical Requirements

- Use a relational database
  - It's ok to use the H2 Database for testing 
  - A postgres DB is running at jdbc:postgresql://localhost:5432/postgres
    - username: postgres
    - password: password
- Use WebMVC for REST
- Use DataJPA for accessing the DB

---

