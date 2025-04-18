# Loan Risk Scoring Engine

A microservice designed to evaluate loan applications based on a dynamic set of rules fetched from a database. It provides endpoints to manage customers, view scoring rules, and submit loan applications for risk assessment.

## Build Instructions

To build the application JAR file, run the following command from the project root:

```bash
./mvnw clean package
```

## Run Instructions

You can run the application using the Maven Spring Boot plugin:

```bash
./mvnw spring-boot:run
```

Alternatively, you can run the packaged JAR file (ensure you have built it first using the build command):

```bash
java -jar target/engine-0.0.1-SNAPSHOT.jar
```
*(Note: Verify the exact JAR filename in the `target/` directory after building)*

## API Endpoints Summary

The following endpoints are available:

*   `POST /customers` - Create a new customer record.
*   `GET /customers/{id}` - Retrieve a customer by their unique ID.
*   `GET /rules` - Retrieve the currently active scoring rules from the database.
*   `POST /loan/apply` - Submit a new loan application for risk scoring.
*   `GET /loan/{id}` - Retrieve a loan application by its unique ID.

## Database Configuration

By default, the application uses an H2 in-memory database for development and testing purposes. The connection string is `jdbc:h2:mem:testdb`.

A PostgreSQL database profile is also available but requires external setup:
1.  Ensure a PostgreSQL server is running and accessible.
2.  Create a database and user for the application.
3.  Update the database connection details (URL, username, password) in the `src/main/resources/application.properties` file.