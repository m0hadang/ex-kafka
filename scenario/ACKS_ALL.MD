# acks=all

- the leader does not send an ACK until all in-sync replicas (ISR) have already replicated the record


### Case 1: Leader crashes **before** followers replicate the record

```
Producer
    |
    | Produce
    v
Leader
    |
    | (not yet replicated)
    X  <-- crashes
Follower1
Follower2
```

Since the record has **not** been replicated to all ISR members:

* The leader **does not send an ACK**.
* The producer eventually receives a timeout or retryable error.
* If **idempotence** is enabled, the producer retries safely.

From the producer's perspective:

```
No ACK = write not completed
```

So the producer never incorrectly believes the record was committed.


### Case 2: Leader replicates to all ISR, then crashes

```
Producer
      |
      | Produce
      v
Leader
  |
  +--> Follower1 ✓
  |
  +--> Follower2 ✓
      |
      +--> ACK Producer
      X crashes
```

Safe. every ISR replica already has the record, Kafka elects one of those ISR replicas as the new leader.

The record is preserved.

### Case 3: Leader crashes immediately after sending the ACK

```
Leader
   |
   +--> ACK Producer
   X crashes
```

Still safe. The ACK was only sent **after** all ISR replicas stored the record.

The new leader already contains the record.

### Why does Kafka use ISR instead of "all replicas"?

Notice that `acks=all` means All In-Sync Replicas (ISR), not Every replica in the cluster.

Example:

```
Replication factor = 3

Leader
Follower1
Follower2
```

Suppose Follower2 is slow.

ISR becomes:

```
Leader
Follower1
```

Now:

```
acks=all
```

only waits for:

```
Leader
Follower1
```

It does **not** wait for the out-of-sync replica.

### Can data still be lost


Suppose:

```
Replication factor = 3

ISR:
Leader
Follower1
```

The leader writes the record.

Follower1 also writes it.

Producer receives ACK.

Then:

```
Leader crashes
Follower1 crashes
```

before any other replica catches up.

Now **every replica that had the record is gone**.

The record is lost.

This is not a failure of `acks=all`; it's because **all replicas that contained the data failed**.

---

### min.insync.replicas

`acks=all` is most effective when combined with:

```
min.insync.replicas=2
```

For example:

```
Replication factor = 3
min.insync.replicas = 2
acks = all
```

If only one replica remains in the ISR:

```
Leader
```

Kafka rejects writes instead of acknowledging them.

This prevents accepting writes that are stored on only a single broker, improving durability.
