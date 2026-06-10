# IBM Bob Kick-Off LAB - Java Modernization | part 2 - CLI
## IBM Bob CLI - 5-10 minutes, single-prompt workflow

This document is **Part 2** of the Java modernization LAB: after exploring and
reasoning about the legacy app in the IDE (`LAB-JAVA-part1-IDE.md`), you switch to
**Bob-CLI** to demonstrate "agentic DevOps" — generating a **runnable Java 21 /
Spring Boot 3 service**, wiring its build and a real PostgreSQL database, and
validating it end to end.

**Key idea:** You use **one single prompt** that instructs the agent to:
1) generate a **Java 21 / Spring Boot 3.x** port of the Aurora Core API
2) migrate `javax.* → jakarta.*` and use **Spring Data JPA / Hibernate 6**
3) target **PostgreSQL** (replacing Oracle), with money as `BigDecimal`/`numeric`
4) add a `docker-compose.yml` that brings up PostgreSQL + a seed script
5) build it (`mvn -q -DskipTests package`), run it, and hit an endpoint to prove it

> **Why this is the strong demo:** the legacy app targets Java 8 / Boot 1.5 and an
> Oracle database most attendees can't stand up on a laptop. The modernized output
> runs on a current JDK against a throwaway PostgreSQL container — so it **actually
> builds and runs in front of the audience**. That is the payoff of the whole
> modernization story.

---

## Pre-flight (30 seconds)

### Assumptions
- You are in the `aurora-bank-legacy/` repository root (the legacy app from Part 1).
- You have a **JDK 21** installed (`java -version`).
- You have **Maven** available (`mvn -version`) or use the generated wrapper.
- You have **Docker** available for the PostgreSQL container (`docker --version`).

### Quick check commands (optional)
```bash
pwd
ls
java -version
mvn -version
docker --version
```

---

## The single prompt (copy/paste)

> Use this **exact** prompt in Bob-CLI.
> If your CLI supports context attachment, attach the schema with
> `@scripts/schema-oracle.sql` and the README with `@README.md`.

#### Simple Prompt

```text
Create a modern Java 21 / Spring Boot 3.x port of this Spring Boot 1.5 banking API
under a new modern/ folder. Migrate javax to jakarta, use Spring Data JPA on
PostgreSQL with money as BigDecimal, keep the same routes and JSON responses, add a
docker-compose.yml for Postgres, then build and run it and show me a working request.
```

#### Complex Prompt

```text
You are an agentic DevOps + Java assistant operating inside the Aurora Core
repository. The current app is Java 8 / Spring Boot 1.5 / Spring Data JPA +
Hibernate 5 (javax.persistence) / Oracle. Your job is to produce a runnable,
modern Java 21 / Spring Boot 3.x version on PostgreSQL and prove it works.

Goal: generate, build, and validate a modern Spring Boot 3 service that talks to a
real PostgreSQL database.

Requirements:
1) Create a modern Maven project under `modern/`:
   - Java 21 (maven.compiler.release=21), Spring Boot 3.x parent + plugin
   - Maven wrapper (mvnw) so it builds without a preinstalled Maven
2) Port the domain and API surface 1:1:
   - Same entities (Customer, Account, LedgerEntry, Transfer) and the double-entry
     ledger model
   - Same routes and JSON shapes as the controllers in
     src/main/java/com/aurorabank/core/web
   - Migrate javax.persistence -> jakarta.persistence (and any other javax -> jakarta)
   - Replace removed Spring Data APIs (CrudRepository.findOne -> findById)
   - Convert field injection to constructor injection
3) Fix correctness + security as part of the port:
   - Money as BigDecimal in entities and services (no primitive double)
   - Make TransferService.transfer @Transactional and add optimistic locking (@Version)
   - Replace the raw concatenated JdbcTemplate query in AccountService with a
     parameterized query
   - Use java.time (Instant/OffsetDateTime) and enums instead of magic strings
4) Replace the database:
   - PostgreSQL via org.postgresql driver + PostgreSQLDialect (not Oracle)
   - Connection settings in application.yml/properties
   - Translate scripts/schema-oracle.sql into modern/db/schema-postgres.sql
     (BINARY_DOUBLE money -> numeric(19,4), NUMBER -> bigint, sequences -> identity,
     TIMESTAMP -> timestamptz, fix identifier casing)
5) Infra + run:
   - Add modern/docker-compose.yml that starts PostgreSQL and applies the schema/seed
   - Add modern/README.md with build/run/test instructions
   - Start Postgres (docker compose up -d), build (mvn -q -DskipTests package), run
     the app, and call GET /api/accounts/10 and GET /api/customers
   - Show a short sample of the JSON responses

Constraints:
- Keep the modern code in `modern/` so old vs new can be diffed; do not break the
  legacy project.
- Preserve the public API contract (routes + JSON field names).
- Prefer standard, idiomatic Spring Boot 3 + Spring Data JPA patterns; keep it
  readable.

Deliverables:
- `modern/` Spring Boot 3 / Java 21 Maven project (jakarta, PostgreSQL)
- `modern/db/schema-postgres.sql`
- `modern/docker-compose.yml`
- `modern/README.md`
- Evidence of successful build + run + sample API responses.
```

---

## What you do live (2–3 minutes)

After Bob-CLI finishes, you run these commands yourself to demonstrate
reproducibility:

```bash
cd modern
docker compose up -d                 # start PostgreSQL
./mvnw -q -DskipTests package        # compile + package the modern service
./mvnw spring-boot:run               # or: java -jar target/*.jar

# in another terminal:
curl http://localhost:8080/api/accounts/10
curl http://localhost:8080/api/customers
```

or, simply hand the follow-up back to the CLI:

```text
Bring up Postgres with docker compose, build the modern project, run the app, and
call GET /api/accounts/10 and GET /api/customers for me. Show the output.
```

**What the audience should see:**
- A modern Spring Boot 3 service compiling on **Java 21** and running against a real
  PostgreSQL container — no Oracle, no legacy JDK.
- Real JSON coming back — the same shapes the legacy API returned.
- Money returned as exact decimals (`BigDecimal` / `numeric`), not float artifacts.

---

## Expected output shape (example)

`GET /api/accounts/10`:

```json
{
  "id": 10,
  "accountNumber": "ACC-1001",
  "customerId": 1,
  "accountType": "CHECKING",
  "currency": "EUR",
  "balance": 2500.00,
  "status": "ACTIVE",
  "openedAt": "2021-03-01T09:00:00Z"
}
```

`GET /api/customers`:

```json
[
  { "id": 1, "fullName": "Anna Schmidt",   "email": "anna.schmidt@example.com",  "status": "ACTIVE" },
  { "id": 2, "fullName": "Marco Rossi",    "email": "marco.rossi@example.com",   "status": "ACTIVE" },
  { "id": 3, "fullName": "Sophie Laurent", "email": "sophie.laurent@example.com","status": "ACTIVE" }
]
```

(Exact values depend on the seed data.)

---

---

## Troubleshooting (quick fixes)

### Wrong Java version / build fails on language level
- Confirm `java -version` is 21. The Boot 3 parent and `maven.compiler.release=21`
  require a JDK 21 toolchain.

### Still seeing `javax.persistence` errors
- Spring Boot 3 uses `jakarta.persistence`. Ensure every entity import was migrated
  and that no dependency drags in the old `javax` JPA API.

### PostgreSQL connection refused
- Ensure the container is up: `docker compose ps`.
- Confirm the datasource URL/credentials match the compose service and that Postgres
  has finished starting before the app connects.

### Relation / column "does not exist"
- PostgreSQL folds unquoted identifiers to lower case. Ensure the JPA entity
  mapping (`@Table`/`@Column`) and `schema-postgres.sql` agree on identifier
  casing/quoting.

### Port already in use
- Change `server.port` (e.g. `--server.port=8081`) or stop the conflicting process.

---

## Optional 60-second enhancements (if you have time)
Ask the CLI agent one more single-line task (only if time allows):

- "Add a `Dockerfile` for the API and add it as a service to `docker-compose.yml` so
  the whole stack — API + PostgreSQL — comes up with one `docker compose up`."
- "Replace the raw SQL schema with a Flyway migration and let it run on startup."
- "Run the Part 1 characterization tests against this modern build to prove
  behavioral equivalence — especially the money math now that balances are
  `BigDecimal`."

Any of these is a strong "modernization + DevOps" closer.
