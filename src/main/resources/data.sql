-- Seed data for SCORING_RULE table

-- Enabled rules based on requirements
INSERT INTO SCORING_RULE (name, field, operator, rule_value, risk_points, priority, enabled) VALUES ('Credit too low', 'creditScore', '<', '600', 30, 10, true);
INSERT INTO SCORING_RULE (name, field, operator, rule_value, risk_points, priority, enabled) VALUES ('Credit average', 'creditScore', '<', '700', 15, 20, true);
INSERT INTO SCORING_RULE (name, field, operator, rule_value, risk_points, priority, enabled) VALUES ('Too young', 'age', '<', '21', 20, 30, true);
INSERT INTO SCORING_RULE (name, field, operator, rule_value, risk_points, priority, enabled) VALUES ('Vacation loan', 'loanPurpose', '==', 'vacation', 10, 40, true);

-- Example disabled rule for testing GET /rules
INSERT INTO SCORING_RULE (name, field, operator, rule_value, risk_points, priority, enabled) VALUES ('Old Disabled Rule', 'age', '>', '90', 5, 50, false);

-- (Optional) Seed data for CUSTOMER table
-- Assuming columns: id, name, email, age, credit_score (Verify if needed)
-- INSERT INTO CUSTOMER (name, email, age, credit_score) VALUES ('Alice Example', 'alice@example.com', 30, 750);
-- INSERT INTO CUSTOMER (name, email, age, credit_score) VALUES ('Bob Test', 'bob@test.com', 25, 580);

-- Note: Uncomment and adjust CUSTOMER inserts if the table/columns exist and are needed for manual testing.
-- The exact column names for CUSTOMER should be verified from Customer.java if uncommenting.