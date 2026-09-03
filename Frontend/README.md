# Wallet Transfer Frontend

React + TypeScript + Vite frontend for the Wallet Transfer MVP.

## Setup

```bash
npm install
cp .env.example .env
npm run dev
```

Set the backend URL in `.env`:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Routes

```text
/wallets/:walletId
/wallets/:walletId/transfer
/wallets/:walletId/transfers
/wallets/:walletId/statement
```

## Build

```bash
npm run build
```

## Tests

```bash
npm test
```

The statement API is intentionally typed to tolerate the two statement-entry shapes present during the backend phase; the UI consumes fields only when they are returned.
