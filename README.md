# Banking MVP (Spring Boot)

A minimal microservice exposing REST APIs for:
- Create customer
- Create savings account
- Deposit
- Withdraw
- Read available balance
- List last N (default 10) transactions

## Tech
- Java 17
- Spring Boot 3.3.x
- Maven
- JUnit 5

## Run
```bash
mvn spring-boot:run
```
Server starts on :8080.

## Docs
```bash
mvn clean generate-resources
```

## Security (OWASP Dependency-Check)
OWASP Dependency-Check scans project dependencies for known security vulnerabilities.
```bash
mvn org.owasp:dependency-check-maven:check
```
An HTML report will be generated at `target/dependency-check-report.html`.

## Example API calls

Create customer:
```bash
curl -s -X POST http://localhost:8080/customers -H 'Content-Type: application/json' -d '{"name":"Alice","email":"alice@example.com"}'
```

Create account:
```bash
curl -s -X POST http://localhost:8080/accounts -H 'Content-Type: application/json' -d '{"customerId":"<CUSTOMER_UUID>"}'
```

Deposit 100:
```bash
curl -s -X POST http://localhost:8080/accounts/<ACCOUNT_UUID>/deposit -H 'Content-Type: application/json' -d '{"amount":100}'
```

Withdraw 50:
```bash
curl -s -X POST http://localhost:8080/accounts/<ACCOUNT_UUID>/withdraw -H 'Content-Type: application/json' -d '{"amount":50}'
```

Balance:
```bash
curl -s http://localhost:8080/accounts/<ACCOUNT_UUID>/balance
```

Last 10 transactions:
```bash
curl -s http://localhost:8080/accounts/<ACCOUNT_UUID>/transactions?limit=10
```

## Tests
```bash
mvn -q -DskipTests=false test
```

## Notes
- In-memory repositories for MVP (no database). Thread-safe maps with per-account synchronization.
- Validation and error handling via `@ControllerAdvice`.
- Amounts use `BigDecimal` with 2 fraction digits; positive amounts enforced.
