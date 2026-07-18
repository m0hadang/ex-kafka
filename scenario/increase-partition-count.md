# increase partition count
- Kafka Streams does **not** redistribute the existing local state stores when the partition count changes.
- The old state remains associated with the old task/partition, while new records are routed according to the new partition mapping.
- **partition assignment change**
  - key moves to ad different partition
  - it is processed by a different task with a different local state store
- Kafka Streams assumes that **all records for a given key always go to the same partition**. Changing the partition count violates that assumption.
- state is partition-local.

```
Key
 ↓
Partition
 ↓
Task
 ↓
State Store
```


### Initially: 2 partitions

```
orders

Partition 0
Partition 1
```

Kafka Streams application:

```
Partition 0  ---> Task 0 ---> State Store 0

Partition 1  ---> Task 1 ---> State Store 1
```

Each **task owns one state store**.

Suppose the key `"Alice"` hashes to partition 0.

```
Alice
   │
   ▼
Partition 0
   │
   ▼
State Store 0
```

Everything for `"Alice"` is stored in State Store 0.

### Now increase to 4 partitions

The topic becomes

```
Partition 0
Partition 1
Partition 2
Partition 3
```

So `"Alice"` may now map to **partition 2**.

```
Alice
   │
   ▼
Partition 2
   │
   ▼
Task 2
   │
   ▼
State Store 2
```

> partition assignment has changed
- The new records for `"Alice"` are now processed by **State Store 2**, while the historical state for `"Alice"` still exists in **State Store 0**.
- partition assignment has changed, the records for `"Alice"` that were previously written to changelog partition 0 are **not automatically moved** into changelog partition 2.
  - As a result, the state for `"Alice"` is not magically transferred to the new partition.

# repartitioning
- repartitioning
  - Creates a new topic and redistributes records into it
  - Every record is rewritten into the new topic
- Most state stores (e.g., created by aggregations, joins, or tables) have an internal **changelog topic**.
- When new tasks are created after repartitioning, Kafka Streams restores each state store by replaying the changelog for **its assigned partition**.

---

# in practice

For stateful Kafka Streams applications:

* **Choose the partition count carefully at the beginning** so it is unlikely to need increasing later.
* If you must increase partitions, it is often treated as a migration:

  * create a new topic with the desired partition count,
  * repartition or reprocess all historical data into the new topic,
  * rebuild the state stores from scratch.

This ensures that all records for a key are replayed into the **correct new state store**.
