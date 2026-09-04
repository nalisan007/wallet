# Wallet Ledger

A small wallet and money-transfer application built around a simple principle: **money should be represented exactly, transfers should be safe under concurrent requests, and every movement of money should leave an auditable ledger entry.**

The project contains a Spring Boot backend and a React frontend. It supports wallet balances, transfers, transfer history, and wallet statements.

This is an MVP, but the core money-movement path is designed around the same concerns that matter in a production system: validation, transactions, idempotency, concurrency control, and an audit trail.

### The prompts.md file has all prompts used to create this project


## Running the backend

### Prerequisites

Install:

- Java 21
- IntelliJ IDEA
- MariaDB or MySQL

If your local database credentials differ from the project's configuration, update the application configuration before starting the backend.

### Start the backend

Clone the repository:

```bash
git clone <repository-url>
```

Open the cloned project directory in IntelliJ IDEA.

Let IntelliJ finish importing the Maven project and downloading its dependencies.

Find the `WalletApplication` class and run its `main` method.

Spring Boot starts the API and Flyway applies the database migrations, including the development seed data.

The backend normally runs on:

```text
http://localhost:8080
```

The backend endpoint index is available at:

```text
http://localhost:8080/
```

If Swagger is enabled in the current configuration, it is available at:

```text
http://localhost:8080/swagger-ui.html
```

## Running the frontend

From the repository root:

```bash
cd Frontend
```

Install the dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Vite prints the local URL in the console. Open the URL shown there, normally:

```text
http://localhost:5173
```

The frontend API base URL is configured through:

```text
Frontend/.env
```

For local development:

```text
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

The `/api/v1` prefix is intentionally part of the configured API base URL. The frontend does not silently add a different API version.

The endpoint page can be opened at:

```text
http://localhost:5173/api/v1
```

A seeded wallet can be opened at:

```text
http://localhost:5173/api/v1/wallets/01999000-0000-7000-8000-000000000004
```

## Testing

Backend tests:

```bash
mvn test
```

Frontend tests:

```bash
cd Frontend
npm test
```

The test suite covers areas including:

- Transfer validation.
- Wallet operations.
- Ledger entry creation.
- Statement calculation.
- Cursor encoding and decoding.
- Idempotency behavior.
- Exception handling.
- Controller behavior.
- Concurrent transfers.
- Concurrent requests using the same idempotency key.



## What the application does

- Maintains wallets with balances stored in paise.
- Transfers money between two active wallets.
- Validates requests before money is moved.
- Makes transfer requests idempotent so retries do not move money twice.
- Protects balances from concurrent transfers.
- Records both sides of every transfer in the ledger.
- Provides cursor-based transfer history.
- Builds wallet statements from ledger entries.
- Includes development seed data so the application can be tested immediately.

## Business rules

### Wallets

- A wallet belongs to a user.
- A user can have at most one wallet.
- A wallet has a status, such as `ACTIVE`.
- Transfers can only use active wallets.
- A wallet balance must never become negative.

### Transfers

A transfer must:

1. Have a valid source wallet.
2. Have a valid destination wallet.
3. Use the same source wallet in the URL and request body.
4. Use two different wallets.
5. Have a positive amount.
6. Have sufficient funds in the source wallet.
7. Update both wallets and the corresponding ledger entries in one transaction.

A successful transfer creates two ledger entries:

```text
Source wallet       DEBIT     1,000 paise
Destination wallet  CREDIT    1,000 paise
```

Both entries reference the same `transferId`, so the complete movement can be traced back to one transfer.

## Validation

Validation is performed at the API boundary and in the service layer where a rule depends on database state.

The application validates, among other things:

- Wallet IDs must be valid UUIDs.
- Transfer amount must be greater than zero.
- Source and destination wallets must be different.
- The URL wallet ID must match `fromWalletId`.
- Both wallets must exist.
- Both wallets must be active.
- The source wallet must have enough balance.
- `Idempotency-Key` must be present and valid.
- `from` must not be later than `to` for date-range queries.
- History page size is bounded.
- Invalid cursors are rejected rather than silently ignored.

## Why amounts use `long`, not `float` or `double`

Money is stored as an integer number of paise:

```text
₹10.50 = 1050 paise
₹0.01  = 1 paise
```

`float` and `double` are binary floating-point types. Values such as `0.1` cannot always be represented exactly, which can introduce rounding errors into financial calculations.

Using `long` keeps the arithmetic exact for the supported range:

```java
long amountPaise = 1050;
```

The application performs calculations using integer paise and only converts to rupees when displaying the value. This avoids floating-point rounding in the transfer path.

## Idempotency

Idempotency protects the transfer endpoint from accidental duplicate processing.

The client sends an `Idempotency-Key` with a transfer request:

```http
Idempotency-Key: 01999000-0000-7000-8000-000000000101
```

Consider a common failure scenario: the server successfully transfers the money, but the network connection drops before the client receives the response. The client retries the request.

Without idempotency, the retry could transfer the same money again.

With idempotency, the retry is associated with the original operation and does not create another transfer.

### What is stored

The application stores:

- The idempotency key.
- A SHA-256 hash of the transfer request.
- The resulting transfer ID once the transfer has completed.

The request hash prevents a client from reusing an existing idempotency key for a different request.

For example, these are not the same operation:

```text
Request A: Wallet A → Wallet B, 1000 paise
Request B: Wallet A → Wallet C, 5000 paise
```

If both use the same idempotency key, the second request is rejected as a conflicting request rather than being treated as a retry of the first one.

### What happens when two requests arrive at the same millisecond?

Suppose two identical requests arrive at almost exactly the same time:

```text
Request 1 ───────┐
                 ├── same Idempotency-Key
Request 2 ───────┘
```

A simple `SELECT` followed by `INSERT` would not be safe. Both requests could perform the `SELECT` before either one performs the `INSERT`, and both could conclude that the key is new.

The application therefore uses a **database-level unique constraint** on the idempotency key together with an atomic insert-if-absent operation.

Conceptually:

```text
Request 1 → INSERT key ──→ succeeds
Request 2 → INSERT key ──→ already exists
```

Only one request can create the idempotency record. The competing request then reads the existing record under a lock and reuses the existing operation/result instead of creating another transfer.

The database constraint is important because application-level checks alone cannot close this race. The database is the final authority that prevents two rows with the same idempotency key.

### Why this works even when the requests arrive at the same time

The application does not depend on timestamps or thread scheduling to decide which request wins. The unique database constraint serializes the competing inserts for the same key.

After the winner creates the idempotency record, the other request observes the existing record and follows the retry path.

The result is therefore:

```text
One logical request → one transfer
Multiple retries    → same transfer/result
```

## Transactions and locking

The transfer operation runs inside a database transaction.

The application uses **pessimistic write locking** when loading the wallets involved in a transfer. This prevents two transactions from simultaneously making balance decisions from the same old wallet balance.

For example, if a wallet has ₹100 and two requests both try to transfer ₹80:

```text
Initial balance: ₹100

Transaction A → locks wallet → sees ₹100 → transfers ₹80 → ₹20
Transaction B → waits for lock → sees ₹20 → fails insufficient-balance check
```

The result is one successful transfer and one failed transfer. The balance cannot become negative because the second balance check happens after the lock is acquired.

### Lock ordering

When a transfer involves two wallets, the application locks them in deterministic UUID order rather than always locking the source first.

Without consistent ordering, two concurrent transfers could theoretically do this:

```text
Transaction A: lock Wallet A → wait for Wallet B
Transaction B: lock Wallet B → wait for Wallet A
```

That creates a deadlock cycle.

The application avoids this pattern by acquiring the wallet locks in the same order for every transfer.

### Isolation level

The application does not override the database's default transaction isolation level. It relies on the database's normal transactional isolation together with explicit pessimistic row locks.

The important protections for the transfer path are:

- One database transaction for the money movement.
- Pessimistic write locks on the wallet rows.
- Deterministic lock ordering.
- Balance validation while the rows are locked.
- Atomic persistence of wallet and ledger changes.
- A database unique constraint for idempotency keys.

This is deliberate: the application does not claim that a higher isolation level alone solves the transfer race. The wallet rows that matter are explicitly locked.

## Ledger design

The wallet balance is stored for fast reads, but the ledger is the audit trail.

A transfer produces two entries:

```text
Transfer #123

Wallet A   DEBIT    5000
Wallet B   CREDIT   5000
```

Both entries reference the same transfer.

The ledger therefore answers basic audit questions such as:

- Which wallet was affected?
- Was money credited or debited?
- How much moved?
- Which transfer caused the entry?
- What transactions make up a wallet's statement?

Statements are calculated from ledger activity rather than assuming that the current wallet balance is the historical balance for every date range.

The application also performs a ledger-versus-wallet balance integrity check after a successful transfer.

## API

All application endpoints are versioned under:

```text
/api/v1
```

### Get wallet

```http
GET /api/v1/wallets/{id}
```

Returns the wallet's current balance, status, and recent activity.

Example:

```text
GET /api/v1/wallets/01999000-0000-7000-8000-000000000004
```

### Create transfer

```http
POST /api/v1/wallets/{walletId}/transfers
```

Required header:

```http
Idempotency-Key: <UUID>
```

Request body:

```json
{
  "fromWalletId": "01999000-0000-7000-8000-000000000004",
  "toWalletId": "01999000-0000-7000-8000-000000000003",
  "amountPaise": 1000
}
```

### Transfer history

```http
GET /api/v1/transfers?walletId={walletId}&from={from}&to={to}&limit=20&cursor={cursor}
```

| Parameter | Required | Description |
|---|---|---|
| `walletId` | Yes | Wallet whose transfers should be returned |
| `from` | No | Start timestamp |
| `to` | No | End timestamp |
| `limit` | No | Number of records to return |
| `cursor` | No | Cursor returned by the previous page |

The endpoint uses cursor-based pagination so clients can continue from the previous page without relying on a large numeric offset.

### Wallet statement

```http
GET /api/v1/wallets/{id}/statement?from={from}&to={to}
```

Returns the ledger-derived activity for the requested period, including opening and closing balances.

## Development seed data

The V2 Flyway migration creates development data so the application can be tested immediately.

### User

```text
01999000-0000-7000-8000-000000000002
```

### User wallet

```text
01999000-0000-7000-8000-000000000004
```

Initial balance:

```text
₹5,000
```

### System wallet

```text
01999000-0000-7000-8000-000000000003
```

Initial balance:

```text
₹50,000
```

The seed balances are represented by opening ledger credit entries as well as the wallet balance.

UUID values in the SQL migration are inserted using `UNHEX(...)` rather than `UUID_TO_BIN(...)`, avoiding a dependency on the latter function.

## Tech stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- Bean Validation
- MariaDB / MySQL
- Flyway
- Jackson
- Maven
- JUnit 5
- Mockito
- Testcontainers

### Frontend

- React
- TypeScript
- Vite
- React Router
- Tailwind CSS
- Vitest

## Architecture

The backend follows a conventional layered structure:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Controllers

Controllers handle HTTP concerns such as path variables, headers, request bodies, validation, and response status. They do not contain the core money-transfer logic.

### Services

Services contain the business rules and transaction boundaries.

`TransferService`, for example, coordinates idempotency, wallet validation, wallet locking, balance validation, wallet updates, transfer creation, ledger creation, and integrity checks.

### Repositories

Repositories contain database access. Operations that participate in concurrent money movement use database locking where required rather than relying on ordinary reads.

## Key architectural decisions

### Integer paise for money

Using `long` paise avoids floating-point rounding in financial calculations.

### Database-backed idempotency

The database unique constraint is used as the final guarantee against duplicate idempotency keys. This is stronger than relying on an application-level `exists()` check.

### Pessimistic wallet locking

Money movement needs a reliable balance check while the balance cannot change underneath the transaction. Pessimistic write locks make that requirement explicit.

### Deterministic lock ordering

Wallets are locked in UUID order to reduce deadlock risk when two transfers touch overlapping wallets.

### Immutable ledger entries

Transfers are represented by ledger entries rather than trying to reconstruct history from changes to a single balance column.

### Cursor pagination

Transfer history uses a cursor so the next page can continue from a known position without depending on a potentially expensive offset.

### Flyway migrations

Database schema and seed data are versioned with the application code. A new environment can recreate the expected schema by running the migration history.

## Project structure

```text
wallet/
├── src/
│   ├── main/
│   │   ├── java/io/wallet/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── db/migration/
│   │       └── application.yml
│   └── test/
│       └── java/io/wallet/
│           ├── controller/
│           ├── exception/
│           └── service/
│
├── Frontend/
│   ├── src/
│   ├── index.html
│   ├── package.json
│   └── vite.config.ts
│
└── README.md
```
## Scope

This is an MVP (Minimum Viable Product) rather than a complete banking platform. Authentication and authorization, production observability, external payment rails, reconciliation processes, operational dashboards, and other production concerns would need to be added before using the system for real financial transactions.

The core transfer path is deliberately kept small and explicit: validate the request, establish idempotency, lock the wallets, check the balance, update both wallets, write the ledger entries, and commit the whole operation as one transaction.
