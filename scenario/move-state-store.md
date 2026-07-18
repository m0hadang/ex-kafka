# move state store

- Consumer rebalance (add/remove instances)	
  - Yes. Ownership moves, and Kafka Streams restores the state on the new instance from a standby replica or changelog.
- Increase topic partition count
  - No. Existing state is not redistributed to match the new partition mapping.
- Kafka Streams repartition	
  - Not exactly. New tasks and new state stores are created and populated from the repartition topic rather than moving the old state stores.


# Case 1: Normal rebalance (adding/removing instances)

Initially:

```text
Instance A

Task 0 -> State Store 0
Task 1 -> State Store 1
Task 2 -> State Store 2
Task 3 -> State Store 3
```

Now you start another instance.

After the rebalance:

```text
Instance A

Task 0 -> State Store 0
Task 1 -> State Store 1

Instance B

Task 2 -> State Store 2
Task 3 -> State Store 3
```

The **ownership** of Task 2 and Task 3 has moved to Instance B.

### What happens to the state store?

Kafka Streams restores the state store on Instance B by:

1. Copying the state from a **standby replica** (if configured), **or**
2. Replaying the **changelog topic** to rebuild the state.

the state store effectively **moves to the new owner**, but Kafka Streams rebuilds or restores it rather than physically copying the RocksDB files between machines.


# Case 2: Increasing topic partitions

Suppose:

```text
2 partitions

P0 -> Alice
P1 -> Bob
```

Increase to 4 partitions.

Now new records may map as:

```text
Alice -> P2
```

There is **no rebalance that moves Alice's existing state from State Store 0 to State Store 2**.

The existing state remains associated with the old partition, because the key-to-partition mapping itself has changed.

> it is not ownership move, but key-to-partition mapping changing

---

# Case 3: Kafka Streams repartitioning

If Kafka Streams creates a repartition topic:

```text
Original Topic
        │
        ▼
Repartition Topic (16 partitions)
        │
        ▼
New Tasks
        │
        ▼
New State Stores
```

The downstream state stores are built from the repartitioned data.

This isn't a "move" of the old state store. It's the creation of **new state stores** corresponding to the repartitioned topic.
