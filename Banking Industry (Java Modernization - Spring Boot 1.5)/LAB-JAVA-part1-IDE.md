# IBM Bob Kick-Off LAB - Java Modernization | part 1
## Case Study: Aurora Core (Spring Boot 1.5 / Java 8 / Spring Data JPA + Hibernate 5 / Oracle)

### Audience
Enterprise Java engineers and architects in **banking / financial services**
maintaining line-of-business applications on **Java 8** and **Spring Boot 1.5**
(`javax.*`, Hibernate 5, Oracle) who are evaluating a move to **Java 21 LTS**,
**Spring Boot 3.x**, and **PostgreSQL**.

### Goal of the LAB
Demonstrate how **IBM Bob** can:
- Understand a non-trivial legacy Java / Spring codebase
- Produce an honest upgrade assessment (blockers, risks, effort)
- Generate concrete engineering artifacts (diagrams, tests, patches, ports)
- Modernize safely: Java 8 → 21, Spring Boot 1.5 → 3.x, `javax.*` → `jakarta.*`
- Migrate the data layer from **Oracle → PostgreSQL**

We use **Aurora Core**, a small core-banking REST API for a retail bank
(`aurora-bank-legacy/`). It is realistic but approachable — customers, accounts,
double-entry ledger postings, and transfers — and it is deliberately built the way a
2016-era enterprise Java app looks today.

> **Why this domain?** Banking is a Java stronghold, and money handling raises the
> stakes: correctness, concurrency, and auditability are not optional. The
> account/transfer domain is instantly familiar to financial-services attendees.

> NOTE: If you have the IBM Bob Java Premium Package, make use of the Java modernization workflow instead of using the provided prompts. Observe the output quality and see the advantage in the speed. 

---

## LAB Flow Overview

1. Understand the code
2. Upgrade assessment & dependency inventory
3. Reliability & correctness analysis
4. Testing & quality gates (safety net before changes)
5. Security & robustness
6. Architecture evolution (Java 8 / Boot 1.5 → Java 21 / Boot 3.x)
7. Database modernization (Oracle → PostgreSQL)
8. Migration plan & cut-over strategy

Each step builds on the previous one and mirrors how a real modernization program
is run: understand → assess → protect → fix → evolve → migrate → plan.

---

## Step A - Understand the Code

### Why this step?
Before modernizing software that moves customers' money, engineers must **fully
understand how it works**:
- Domain model and transfer / ledger semantics
- Data-access strategy and transaction boundaries
- Configuration and runtime assumptions
- What couples it to Java 8, Spring Boot 1.5, `javax.*`, and Oracle

This step shows that Agentic AI can perform a **deep technical read**, not just
summarize files.

### Prompt
```bash 
Can you describe what this application does and how it is structured. Show me the
software architecture as a Mermaid diagram, and explain the money-transfer flow.
```

#### Enhance the prompt with Bob's magic wand, and you should see something like this:
```bash 
Analyze the Aurora Core codebase and provide a comprehensive explanation of its
functionality: the customer/account/ledger/transfer domain model, the double-entry
posting model (each transfer produces a DEBIT and a CREDIT `LedgerEntry`), the
account and transfer lifecycles, how money is moved in `TransferService`, how
interest is posted in `InterestService`, how data is accessed via Spring Data JPA /
Hibernate 5 and a raw `JdbcTemplate`, and how the app is configured for Oracle in
`application.properties`. Create a detailed Mermaid diagram showing the layers
(`@RestController` → `@Service` → Spring Data repositories → Hibernate → Oracle),
the entity relationships, and the request flow for opening an account and posting a
transfer. Explicitly call out everything that couples this application to Java 8,
Spring Boot 1.5, the `javax.persistence` namespace, and Oracle, and state the key
invariants and assumptions the code relies on (especially around money and balances).
```
---

## Step B - Upgrade Assessment & Dependency Inventory

### Why this step?
Leadership needs an **honest, evidence-based assessment** before committing budget:
what is the target, what blocks the move, which dependencies have modern
equivalents, and where the effort and risk concentrate. This is the Java-specific
heart of the engagement.

### Prompt
```bash 
Produce a Java modernization assessment for this application. Inventory the
framework and library dependencies and tell me their status on Java 21 and
Spring Boot 3.
```

#### Enhance the prompt with Bob's magic wand:
```bash 
Act as a Java modernization architect. Produce an upgrade assessment that moves this
app from Java 8 / Spring Boot 1.5 to Java 21 LTS / Spring Boot 3.x. Inventory the
`pom.xml` (Spring Boot parent 1.5.22, starters, `ojdbc8`, H2) and the source-level
framework couplings, and classify each as: supported as-is, needs an upgrade or
replacement (name the modern equivalent), or hard blocker. Call out specifically:
the **`javax.* → jakarta.*` namespace migration** (`javax.persistence`,
`javax.sql.DataSource`) required by Spring Boot 3 / Jakarta EE 9+; Spring Boot 1.5
API removals (e.g. `CrudRepository.findOne` → `findById`, property/config changes);
the Spring 4 → 6 changes; Java 8 → 21 language and JVM considerations; the build
upgrade (Boot parent + plugin, `maven.compiler.release`); JUnit 4 → JUnit 5; and the
data-access change (Oracle dialect / `ojdbc` → PostgreSQL / Npgsql-equivalent
`org.postgresql` driver and dialect). Rank the work by risk and effort (T-shirt
sizes), list the top blockers, and recommend a target approach. Do not change any
code yet — this step is assessment only.
```
---

## Step C - Reliability & Correctness

### Why this step?
A core-banking system must never lose money, double-spend, or post inexact amounts.
Before and during modernization we must understand the correctness risks that the
legacy code hides — and money handling is the headline risk.

### Prompt
```bash
Review the transfer, fee, and interest logic for correctness, money-precision, and
concurrency problems. Where can the system lose money or let two requests spend the
same balance?
```

#### Enhance the prompt with Bob's magic wand:
```bash
Audit `TransferService.transfer`, `AccountService.chargeFee`, and
`InterestService.postMonthlyInterest` for correctness, money-precision, and
concurrency hazards. Identify: the use of primitive **`double` for money** (balances,
amounts, interest) and the floating-point rounding errors this causes — recommend
`BigDecimal` or integer minor units; the **lost-update race condition** in `transfer`
(read-check-write on balances with no locking, optimistic version, or DB
constraint) that allows two concurrent transfers to overdraw an account; the
**missing `@Transactional` boundary** in `transfer` (debit, credit, and two ledger
inserts can partially fail and lose money); the non-atomic interest posting; the use
of `java.util.Date` and a shared non-thread-safe static `SimpleDateFormat`; and the
magic-string `status` fields. Rank issues by severity and likelihood and propose
minimal, framework-appropriate fixes (`@Transactional`, optimistic locking with
`@Version`, `BigDecimal` money, `java.time`, enums). Provide the fixes as a patch or
diff.
```
---

## Step D - Testing & Quality Gates

### Why this step?
You cannot safely refactor or re-platform code you cannot test. A
**characterization test suite** captures today's behavior so we can prove the
modernized app still behaves the same — and lets us pin down the money math.

### Prompt
```bash 
Create a test suite that pins down the current behavior of transfers, fees, and
interest, so we have a safety net before modernizing.
```

#### Enhance the prompt with Bob's magic wand:
```bash 
Create a characterization (golden-master) test suite for the transfer, fee, and
interest logic. Cover: a successful transfer (both balances updated, two ledger
entries written, transfer marked `POSTED`), rejection on insufficient funds,
rejection when an account is not `ACTIVE`, fee charging, and one month of interest
posting with the expected amounts. Use an in-memory database (H2) and Spring Boot
test slices so the tests run without Oracle, assert the **exact** expected balances
and amounts (so floating-point behavior is visible and pinned), and structure the
tests so the same assertions can later run against the modernized Java 21 /
Spring Boot 3 code to prove behavioral equivalence. Provide the test class(es) and
instructions to run them with Maven.
```
---

## Step E - Security & Robustness

### Why this step?
Banking code is a high-value target. Legacy data-access code frequently contains
injection and input-handling flaws, and modernization is the right moment to close
them.

### Prompt
```bash 
Audit the data-access and API code for security issues, especially SQL injection,
and fix them.
```

#### Enhance the prompt with Bob's magic wand:
```bash 
Perform a security review focused on the data layer and API. In particular, analyze
`AccountService.searchByCustomerName`, which concatenates the `name` request
parameter directly into a raw `JdbcTemplate` SQL string — demonstrate the SQL
injection risk with a concrete malicious `name` value, then fix it using a
parameterized query (`?` placeholder + argument) and note the EF-Core-equivalent
parameterization once on PostgreSQL. Also review: the transfer endpoint returning
raw exception messages to the client, missing input validation on amounts and
parameters (negative/zero/overflow), the absence of authentication/authorization on
money-moving endpoints, and any other OWASP-relevant issues for a banking API. Rank
findings by severity and provide the fixes as a patch or diff.
```
---

## Step F - Architecture Evolution (Java 8 / Boot 1.5 → Java 21 / Boot 3.x)

### Why this step?
This is the core modernization move: bring the app onto a current, supported runtime
and framework — Java 21, Spring Boot 3.x, the Jakarta namespace, constructor
injection, and `java.time` — while preserving the public API contract.

### Prompt
```bash 
Modernize this application to Java 21 and Spring Boot 3.x: migrate `javax` to
`jakarta`, update Maven, switch to constructor injection, and replace removed APIs.
Keep the same routes and JSON responses.
```

#### Enhance the prompt with Bob's magic wand:
```bash 
Modernize Aurora Core from Java 8 / Spring Boot 1.5 to Java 21 LTS / Spring Boot 3.x,
preserving the existing API surface (same routes, same JSON shapes). Specifically:
update `pom.xml` to the Spring Boot 3.x parent, `maven.compiler.release=21`, and
current dependencies; perform the **`javax.persistence` → `jakarta.persistence`**
(and any other `javax.* → jakarta.*`) namespace migration; replace removed Spring
Data APIs (`CrudRepository.findOne` → `findById(...).orElse(null)` or proper
`Optional` handling); convert field injection to **constructor injection**; replace
`java.util.Date` / `SimpleDateFormat` with `java.time` (`Instant`/`OffsetDateTime`);
introduce `BigDecimal` for money and enums for status as identified in Step C; and
apply the correctness and security fixes from Steps C and E. Keep Oracle for now (we
migrate the database in the next step). Explain each change and keep the modernized
code in a clearly separated folder so we can diff old vs new.
```
---

## Step G - Database Modernization (Oracle → PostgreSQL)

### Why this step?
The bank wants PostgreSQL. With the app on Spring Boot 3 / Hibernate 6, switching
databases is tractable — but there are real dialect, type, sequence, and identifier
differences to handle, and money columns deserve a correct type.

### Prompt
```bash 
Migrate the data layer from Oracle to PostgreSQL. Provide a Postgres schema, update
the driver and dialect, and explain the differences from the Oracle version.
```

#### Enhance the prompt with Bob's magic wand:
```bash 
Migrate the modernized app's data layer from Oracle to PostgreSQL. Swap the JDBC
driver (`ojdbc8` → `org.postgresql:postgresql`), connection URL, and Hibernate
dialect (`Oracle12cDialect` → `PostgreSQLDialect`); translate
`scripts/schema-oracle.sql` into a PostgreSQL schema, accounting for the differences
(`NUMBER(19)` → `bigint`, **`BINARY_DOUBLE` money columns → `numeric(19,4)`**,
`VARCHAR2` → `varchar`, `TIMESTAMP` → `timestamptz` in UTC, the Oracle
`HIBERNATE_SEQUENCE` / sequence-based identity → PostgreSQL `GENERATED ... AS
IDENTITY` or a sequence, and case-folding of unquoted identifiers); align the JPA id
generation strategy with PostgreSQL; replace the raw `JdbcTemplate` query with a
parameterized PostgreSQL-safe equivalent; and provide a Flyway/Liquibase migration
plus a docker-compose snippet to spin up PostgreSQL locally. List the data-migration
considerations for moving existing rows from Oracle to PostgreSQL (including
converting `double` balances to `numeric`), and note any behavioral differences to
verify with the Step D tests.
```
---

## Step H - Migration Plan & Cut-Over Strategy

### Why this step?
A bank needs a credible, incremental, auditable path to production — not a risky
big-bang rewrite of a money-moving system.

### Prompt
```bash 
Give me a phased modernization and cut-over plan for moving this app and its
database to production on Java 21 / Spring Boot 3.x and PostgreSQL.
```

#### Enhance the prompt with Bob's magic wand:
```bash 
Produce a phased, low-risk modernization roadmap to take Aurora Core to production on
Java 21 / Spring Boot 3.x and PostgreSQL. Cover: sequencing (assessment → test
safety net → in-place correctness/security fixes → Java 21 + Boot 3 + Jakarta
upgrade → Oracle → PostgreSQL → containerize/deploy); where a strangler-fig approach
lets old and new run side by side behind an API gateway; the data-migration,
reconciliation, and dual-write/back-fill options for the Oracle → PostgreSQL
cut-over and how to validate balances and ledger totals to the cent; how the Step D
characterization tests gate each phase; rollback strategy for a money-moving system;
and the CI/CD and containerization changes (Dockerfile, build pipeline) needed to
deploy on Linux. Summarize as a milestone plan with risks and exit criteria per
phase.
```
---


