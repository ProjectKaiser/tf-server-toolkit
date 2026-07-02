# Fault localization: POS freeze when payment is started

Status: localized (static analysis only; not yet reproduced/verified)

## Symptom

Starting a payment hangs the POS. Server logs around incidents show connection
pool exhaustion and a masking ConcurrentModificationException in the pool.

## Localized fault

File: src/com/triniforce/server/plugins/kernel/SrvSmartTranFactory.java
Method: pop(), lines 63-82

`trn.close()` is called before `pool.returnConnection(con)`, and
`returnConnection` is NOT inside a finally that covers `trn.close()`. When
`trn.close()` throws, control jumps straight to the `finally { ApiStack.popApi(); }`
block, so `returnConnection` is skipped. The api entry is popped but the JDBC
connection is never handed back to DBCP2 -> it stays "active" forever (leak).

```text
SrvSmartTranFactory.pop():
    trn.close();                    <-- fault: may throw; if it does ...
    ...
    pool.returnConnection(con);     <-- ... this is skipped (not in finally)
  finally:
    ApiStack.popApi();              <-- only the api stack is unwound
```

## Why trn.close() can throw

- src/com/triniforce/db/dml/SmartTran.java close(boolean) (lines 66-84):
  no-arg `close()` -> `close(false)` -> `m_conn.rollback()`, wrapped with
  `ApiAlgs.rethrowException(e)`. A failed rollback (broken connection, Firebird
  fatal error) is rethrown as a RuntimeException.
- src/com/triniforce/server/plugins/kernel/SrvSmartTran.java close(boolean)
  (lines 50-118): captures the first inner ITranExtender.pop() failure in
  `eFirstProblem` and rethrows it at the end (lines 113-115). So a misbehaving
  inner extender makes close() throw even when the connection itself is healthy.

## Causal chain to the symptom

```text
POS starts payment
      |
      v
BasicServerInvoker.invokeService(): enterMode(Running) -> push() acquires connection
      |
      v
service body throws OR commit deferred; finally -> leaveMode() -> SrvSmartTranFactory.pop()
      |
      v
trn.close() throws (rollback fails OR inner extender pop() rethrown as eFirstProblem)
      |
      v
pool.returnConnection(con) skipped   <-- connection leaked (never returned to DBCP2)
      |
      v
leaked connections accumulate -> pool reaches maxTotal
      |
      v
later push() -> getPooledConnection() blocks up to maxWaitMillis   (POS freeze)
      |
      v
on timeout SQLException -> getInfo() iterates unsynchronized HashMap -> CME (masking log)
```

## Why this matches "freeze on payment"

Payments are write transactions and the most likely to hit rollback/commit-time
faults and extender failures, so they disproportionately trigger the throwing
`close()` path - each occurrence leaks one connection until the pool is drained.

## Consistency check

A leaked connection is also never removed from `TFPooledConnection.m_conStack`
(because `returnConnection`'s `m_conStack.remove(con)` never runs), so the
leaked holders pile up in the same map that `getInfo()` later iterates - matching
the observed CME location.

## Secondary issue (not the freeze cause)

`TFPooledConnection.getInfo()` / `getTakenConnectionPoints()` iterate `m_conStack`
without synchronization while other threads mutate it under `synchronized(this)`.
This only masks the informative EPoolConnectionError with a CME; it does not
itself leak connections.

## Recommended fix direction

Make `pop()` release the connection unconditionally, e.g. move
`pool.returnConnection(con)` into a `finally` that wraps `trn.close()`, so a
throwing close still returns the connection. Then address the secondary CME by
making the `m_conStack` reads in `TFPooledConnection` thread-safe.

## Open questions / verification (for uimpl)

- Confirm running server's BasicDataSource `maxTotal` / `maxWaitMillis` /
  `blockWhenExhausted`.
- Identify which inner/outer ITranExtenders are registered in production and
  whether their pop() can throw during payment.
- Add a regression test: pop() with a throwing trn.close() must still call
  returnConnection (extend SrvSmartTranFactoryTest2).
