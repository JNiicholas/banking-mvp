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

## Run with Docker Compose
```bash
docker-compose up --build
```
This starts the application and any dependent services defined in `docker-compose.yml`.

## Docs
```bash
mvn clean generate-resources
```

## Checkstyle

Run Checkstyle to enforce coding standards:

```bash
# Run Checkstyle and fail the build on violations
mvn checkstyle:check

# Generate a style report (does not fail the build)
mvn checkstyle:checkstyle
```

## Security (OWASP Dependency-Check)
## Keycloak

- **Admin Console:** http://localhost:8081/admin/
- **Realm export JSON:** see `config/keycloak/realm-export` — this file contains the Keycloak realm configuration used by this project.



**OWASP Dependency-Check** identifies known CVEs in third‑party dependencies by analyzing project artifacts (Maven, Gradle, etc.) and matching them to NVD/CPE data. It produces an HTML/JSON/XML report.

### How to run (Maven)
```bash
# Run a scan and generate a report
mvn -U org.owasp:dependency-check-maven:check

# (Optional) Only update the local vulnerability database
mvn org.owasp:dependency-check-maven:updateonly
```

### Where to find the report
- HTML: `target/dependency-check-report.html`
- JSON: `target/dependency-check-report.json`
- XML:  `target/dependency-check-report.xml`

### Useful plugin configuration (pom.xml)
```xml
<plugin>
  <groupId>org.owasp</groupId>
  <artifactId>dependency-check-maven</artifactId>
  <version>12.1.6</version>
  <executions>
    <execution>
      <goals>
        <goal>check</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <!-- Fail the build if a vulnerability with CVSS >= 7.0 is found -->
    <failOnCVSS>7.0</failOnCVSS>
    <!-- Optionally point to a suppression file for false positives -->
    <!-- <suppressionFiles>
         <suppressionFile>dependency-check-suppressions.xml</suppressionFile>
       </suppressionFiles> -->
    <!-- Use an NVD API key from environment (see below) -->
    <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>
  </configuration>
</plugin>
```

### Obtain and use an NVD API Key
1. Request a key from NVD (you need an account).
2. Store it as an environment variable, e.g. `NVD_API_KEY`.
3. Expose it to the plugin via `<nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>` as shown above.


**Official resources**
- Dependency-Check docs (Maven): https://jeremylong.github.io/DependencyCheck/dependency-check-maven/index.html
- Project repository: https://github.com/jeremylong/DependencyCheck
- NVD API key request: https://nvd.nist.gov/developers/request-an-api-key


## Project Lombok

Project Lombok reduces Java boilerplate (getters/setters, constructors, builders, logging) by generating code at **compile time** via annotation processing. Lombok hooks into the Java compiler and modifies the **javac Abstract Syntax Tree (AST)** (often called the *compile tree*). When you annotate a class (e.g., with `@Getter`, `@Setter`, `@Builder`, `@Value`), Lombok injects the corresponding members into the AST *before* bytecode is written, so the generated methods/constructors are present in the compiled classes but not in your source files.

**Key points**
- **Annotation processor**: Lombok runs as a compile-time processor that alters the AST. IDEs must enable **annotation processing** and usually need the **Lombok plugin** for correct code insight and navigation.
- **Common annotations**: `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, `@RequiredArgsConstructor`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Builder`, `@Value`, `@Data`, and loggers like `@Slf4j`.
- **Delombok**: To inspect generated code or for tools that require explicit sources, use *delombok* to materialize the generated members into plain Java sources.

**Official resources**
- Lombok home: https://projectlombok.org
- Feature overview: https://projectlombok.org/features
- IDE setup / annotation processing: https://projectlombok.org/setup/overview
- Delombok: https://projectlombok.org/features/delombok



## Example API calls (Only Examples - Requires valid token)

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
- Uses an Postgres database for data persistence.
- Basic CRUD APIs for customers and accounts, with transaction support (deposit/withdraw).
- Validation and error handling via `@ControllerAdvice`.
- OWASP Dependency-Check integrated into the build for security scanning.
- Project Lombok used for reducing boilerplate code.
