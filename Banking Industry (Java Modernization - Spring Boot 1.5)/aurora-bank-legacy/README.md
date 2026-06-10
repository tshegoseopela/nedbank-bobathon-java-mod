# Aurora Core (Legacy) — Spring Boot 1.5 / Java 8 Banking API

A small but realistic **core-banking** REST API for a retail bank. This is the
**"before"** codebase used in the IBM Bob Java Modernization LAB — the thing we
modernize *from*.

It is deliberately built the way a 2016-era enterprise Java app looks today:

| Concern            | This app uses (legacy)                                       |
|--------------------|--------------------------------------------------------------|
| Language           | Java 8                                                       |
| Framework          | Spring Boot **1.5.22** (Spring 4.x, Tomcat 8)                |
| Persistence        | Spring Data JPA + Hibernate 5, **`javax.persistence`**        |
| Build              | Maven (`spring-boot-starter-parent` 1.5)                     |
| Database           | **Oracle** (`ojdbc8`, `Oracle12cDialect`)                    |
| Dates              | `java.util.Date` + static `SimpleDateFormat`                 |
| DI style           | Field injection (`@Autowired` on fields)                     |
| Money              | Stored and computed as primitive `double`                    |
| Tests              | JUnit 4 (via Boot 1.5 starter)                               |

## Domain

A retail bank moving money between customer accounts:

- **Customer** — a bank customer.
- **Account** — a CHECKING or SAVINGS account with a balance, currency, status.
- **LedgerEntry** — a double-entry posting (DEBIT / CREDIT) against an account.
- **Transfer** — funds movement between two accounts.

Transfer lifecycle: `PENDING → POSTED` (or `REVERSED`).
Account status: `ACTIVE → FROZEN → CLOSED`.

## Key API endpoints

```
GET  /api/customers
GET  /api/customers/{id}
POST /api/customers

GET  /api/accounts/{id}
GET  /api/accounts/customer/{customerId}
POST /api/accounts?customerId=1&type=SAVINGS&currency=EUR
GET  /api/accounts/search?name=smith
POST /api/accounts/{id}/fee?amount=5.00

POST /api/transfers          (JSON body: fromAccountId, toAccountId, amount, memo)
```

## Running it

> Targets Java 8 and Spring Boot 1.5. Production runs against **Oracle**.

1. Create an Oracle schema named `AURORA` and run `scripts/schema-oracle.sql`.
2. Confirm the datasource settings in
   `src/main/resources/application.properties`.
3. Build and run:
   ```bash
   ./mvnw spring-boot:run        # or: mvn spring-boot:run
   ```
4. Browse to e.g. `http://localhost:8080/api/accounts/10`.

> To run **without** Oracle, switch to the commented-out H2 fallback block in
> `application.properties` (note: the H2 schema is created by Hibernate
> `ddl-auto=create-drop` and is empty unless you add seed data).

## Why this is a good modernization candidate (for the LAB)

The app intentionally contains issues an agent can find and fix:

- **Money as `double`** in `Account`, `LedgerEntry`, `Transfer`, and interest
  math — floating-point rounding is unacceptable for currency (should be
  `BigDecimal` / minor units).
- **Concurrency / lost-update** risk in `TransferService.transfer`: read-check-write
  on balances with no locking or optimistic concurrency → double-spend.
- **No transaction boundary**: `transfer` debits, credits, and writes two ledger
  rows without `@Transactional` → partial transfer can lose money.
- **SQL injection** in `AccountService.searchByCustomerName` (string-concatenated
  JDBC query).
- **Non-thread-safe** static `SimpleDateFormat` used to build account numbers.
- **`javax.persistence`** namespace (must become `jakarta.persistence` on Spring
  Boot 3 / Jakarta EE 9+).
- **Field injection**, magic-string statuses, `java.util.Date` instead of
  `java.time`, deprecated `CrudRepository.findOne` (removed after Boot 1.5).

See `../LAB-JAVA-part1-IDE.md` and `../LAB-JAVA-part2-CLI.md`.
