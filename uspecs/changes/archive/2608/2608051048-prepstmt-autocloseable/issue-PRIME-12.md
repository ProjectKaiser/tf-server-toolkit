# TF Server Toolkit: make `PrepStmt` be `AutoCloseable`

- URL: https://untill.atlassian.net/browse/PRIME-12
- ID: PRIME-12
- State: In Progress
- Author: Denis Gribanov
- Assignees: Denis Gribanov
- Labels: none
- Parent: PRIME-3 - Dorhout Mees: POS freezes when payment is started

## Description

There are 3 similar places: [SyncUtils](https://github.com/untillpro/UBL/blob/361d72c6a20b5d8a44a51d3f245d77307721d7e7/src/main/java/eu/untill/ubl/sync/SyncUtils.java#L16), [UntillDb](https://github.com/untillpro/UBL/blob/361d72c6a20b5d8a44a51d3f245d77307721d7e7/src/main/java/eu/untill/ubl/sync/UntillDb.java#L98), [Generators](https://github.com/untillpro/UBL/blob/361d72c6a20b5d8a44a51d3f245d77307721d7e7/src/main/java/eu/untill/ubl/db/Generators.java#L19)

For example, now `SyncUtils.enableLogging()` and `.disableLogging()` code calls this code:

```java
private static void executeAndClose(String sql, String profileItem) {
  StmtContainer sc = SrvApiAlgs2.getStmtContainer();
  PrepStmt ps = sc.prepareStatement(sql, profileItem);
  Throwable primary = null;
  try {
      ps.execute();
  } catch (Throwable t) {
      primary = t;
      throw t;
  } finally {
      try {
          ps.close();
      } catch (Throwable closeEx) {
          if (primary != null) {
              primary.addSuppressed(closeEx);
          } else {
              throw closeEx;
          }
      }
  }
}
```

If make `PrepStmt` be `AutoCloseable` then:

```
private static void executeAndClose(String sql, String profileItem) {
  StmtContainer sc = SrvApiAlgs2.getStmtContainer();
  try (PrepStmt ps = sc.prepareStatement(sql, profileItem)) {
      ps.execute();
  }
}
```
