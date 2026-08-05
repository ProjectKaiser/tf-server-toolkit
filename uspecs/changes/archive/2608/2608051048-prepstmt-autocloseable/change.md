---
change_id: 2608051037-prepstmt-autocloseable
type: refactor
scope: [db-dml, tests]
issue_url: https://untill.atlassian.net/browse/PRIME-12
---

# Change request: Try-with-resources support for prepared statements

Refs:

- [PRIME-12: TF Server Toolkit: make `PrepStmt` be `AutoCloseable`](./issue-PRIME-12.md)

## Why

Manual exception-safe closing creates verbose, error-prone boilerplate at several prepared-statement call sites. Supporting Java's standard resource-management construct simplifies reliable cleanup while preserving statement execution semantics.

## What

This refactor introduces no runtime behavior change:

- Prepared statements are compatible with Java try-with-resources and close when their resource scope exits.
- Existing statement preparation, execution, and result behavior remains unchanged.
- Explicit closing and suppressed-exception behavior remain unchanged.
- Existing callers remain source-compatible; the API change is additive.

## How

Decisions:

- Add the resource-management contract only to the prepared-statement wrapper rather than broadening the generic statement or container APIs.
- Use the inherited statement close lifecycle as the contract implementation instead of introducing a second cleanup path.
- Verify the contract and owner-detachment lifecycle through the existing DML test layer under the project's Java 8 toolchain.

Assumptions:

- None

Out of scope:

- Making generic statements, statement containers, transactions, or result-set wrappers auto-closeable.
- Migrating downstream UBL call sites to try-with-resources.

References:

- [prepared-statement wrapper](../../../../../src/com/triniforce/db/dml/PrepStmt.java)
- [shared statement close lifecycle](../../../../../src/com/triniforce/db/dml/Stmt.java)
- [statement ownership and detachment](../../../../../src/com/triniforce/db/dml/StmtContainer.java)
- [DML lifecycle test coverage](../../../../../test/com/triniforce/db/dml/SmartTranTest.java)
- [Java 8 build contract](../../../../../build.gradle)

## Construction

- [x] update: [db/dml/SmartTranTest.java](../../../../../test/com/triniforce/db/dml/SmartTranTest.java)
  - add coverage that a prepared statement can be used as a try-with-resources resource and remains usable inside the resource scope
  - verify leaving the resource scope closes the statement and removes it from its container's tracked statements

- [x] update: [db/dml/PrepStmt.java](../../../../../src/com/triniforce/db/dml/PrepStmt.java)
  - declare the prepared-statement wrapper as implementing `AutoCloseable`
  - satisfy the contract through the inherited public `close()` method without duplicating or changing cleanup logic
