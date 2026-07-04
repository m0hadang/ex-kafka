# aggregate

- calculating sums, counts, or custom aggregations
- require state stores to maintain running totals
- ex)
```kt
KTable<String, Long> userLoginCounts = loginEvents
    .groupByKey()
    .count(Materialized.as("user-login-counts"));
```