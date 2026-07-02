---
change_id: 2606301356-investigate-pos-freeze-on-payment
type: fix
scope: [transactions, connection-pool]
issue_url: https://dev.untill.com/projects/#!775455
---

# Change request: Investigate POS freeze when payment is started

## Why

POS clients freeze when an operator starts a payment, blocking sales at the
point of sale. Static analysis localized the cause to `SrvSmartTranFactory.pop()`:
it returns the pooled connection only if `trn.close()` succeeds, so a throwing
close (failed rollback or a rethrowing transaction extender) leaks the connection.
Leaked connections accumulate until the pool reaches `maxTotal`, after which new
requests block on connection acquisition (the freeze); the
ConcurrentModificationException seen in the logs is a downstream masking symptom.
Fixing the release path stops the leak and restores the POS under load.

## What

Symptom: Starting a payment on the POS makes the client hang with no response.

```text
POS client starts payment
      |
      v
BasicServerInvoker.invokeService(): enterMode(Running) -> push() acquires connection
      |
      v
payment transaction ends; leaveMode() -> SrvSmartTranFactory.pop()
      |
      v
trn.close() throws (failed rollback        <-- fault: pop() then skips
or rethrown transaction extender)              pool.returnConnection() (not in finally)
      |
      v
connection never returned to pool (leaked); repeats until pool reaches maxTotal
      |
      v
next payment: getPooledConnection() blocks up to maxWaitMillis; POS receives no response   (symptom)
```

See [fault.md](./fault.md) for the full causal chain, evidence, and the
downstream ConcurrentModificationException that masks the pool diagnostic.

Corrected behavior: Starting a payment does not block the POS; the server
acquires a database connection promptly because pooled connections are released
in a timely manner, and on genuine exhaustion the request fails fast with a
clear diagnostic instead of hanging.

## How

Decisions:

- Treat this as a two-stage change: first statically localize why pooled
  connections stay checked out by auditing every `push()`/`pop()` and
  `getPooledConnection()`/`returnConnection()` pairing for paths that acquire a
  connection but fail to release it, then fix the identified holder.
- Make the connection-metadata access thread-safe in `TFPooledConnection`
  (`getInfo()` and `getTakenConnectionPoints()`), aligned with the existing
  `synchronized(this)` used by `getPooledConnection`/`returnConnection`, so the
  masking `ConcurrentModificationException` no longer replaces
  `EPoolConnectionError` and the diagnostic that names connection holders
  survives.
- Confirm the running server's `BasicDataSource` settings (`maxTotal`,
  `maxWaitMillis`, `blockWhenExhausted`) to quantify how long request threads
  block before failing.

Out of scope:

- Migrating Firebird to 64-bit (infrastructure change tracked separately).
- Reworking the connection-pool or transaction architecture beyond the
  localized fault.

References:

- [pooled connection wrapper and getInfo()](../../../../../src/com/triniforce/server/plugins/kernel/TFPooledConnection.java)
- [transaction push/pop that acquires and releases connections](../../../../../src/com/triniforce/server/plugins/kernel/SrvSmartTranFactory.java)
- [pooled connection interface](../../../../../src/com/triniforce/server/srvapi/IPooledConnection.java)
- [server enterMode that triggers push()](../../../../../src/com/triniforce/server/plugins/kernel/BasicServer.java)
- [pool error and thread-safety tests](../../../../../test/com/triniforce/server/plugins/kernel/SrvSmartTranFactoryTest2.java)
- [Apache Commons DBCP2 BasicDataSource configuration](https://commons.apache.org/proper/commons-dbcp/configuration.html)

## Construction

- [x] update: [kernel/SrvSmartTranFactoryTest2.java](../../../../../test/com/triniforce/server/plugins/kernel/SrvSmartTranFactoryTest2.java)
  - add regression coverage for `SrvSmartTranFactory.pop()` when `ISrvSmartTran.close()` throws: verify the pooled `Connection` is still returned and the API stack is popped
  - add coverage that a pool acquisition failure still reports the already-held connection points in `EPoolConnectionError`
  - add coverage that pool diagnostics can be read while connections are returned without surfacing `ConcurrentModificationException`

- [x] update: [kernel/SrvSmartTranFactory.java](../../../../../src/com/triniforce/server/plugins/kernel/SrvSmartTranFactory.java)
  - capture the current `IPooledConnection` and `Connection` before closing the smart transaction
  - return the connection in a `finally` path so `trn.close()` failures do not leak checked-out pooled connections
  - preserve the transaction-close failure for callers after attempting connection return and keep `ApiStack.popApi()` in the outer cleanup path

- [x] update: [kernel/TFPooledConnection.java](../../../../../src/com/triniforce/server/plugins/kernel/TFPooledConnection.java)
  - synchronize `getInfo()` access to `m_conStack` with the same monitor used by checkout and return
  - return a stable snapshot from `getTakenConnectionPoints()` so diagnostics cannot iterate over a mutating backing collection
  - keep existing `BasicDataSource` diagnostic values (`maxTotal`, `numActive`, `maxWaitMillis`) intact
