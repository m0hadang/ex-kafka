### Message Lose(acks=all)

kafka could lose message even if using `acks=all`

### Scenario 1: Leader writes to disk, then crashes

Suppose:

```text
Replication Factor = 3

Leader
Follower1
Follower2
```

The producer sends:

```text
Message A
```

The leader writes it to its local log:

```text
Leader
---------
Offset 100 : Message A   ✓
```

But **Follower1** and **Follower2** have not replicated it yet.

Then the leader crashes.

### What happens next?

Kafka must elect a new leader.

The old leader is dead.

So Kafka chooses one of the ISR replicas, for example:

```text
Follower1 -> New Leader
```

But Follower1's log is:

```text
Follower1
---------
Offset 99
```

It never received Message A.

So from the cluster's point of view:

```text
Offset 100 never existed.
```

The record disappears.

The old leader's disk still contains Message A, but that broker is no longer the leader.


### Why not recover it from the old leader?

Because Kafka's durability guarantee comes from **replication**, not from one broker's disk.

Imagine the leader's machine:

```text
Leader
  SSD
  CPU
  RAM
```

If:

* the SSD dies,
* the machine is destroyed,
* or the disk becomes unreadable,

then the only surviving copies are on the followers.

If no follower has the record, Kafka cannot recover it.

### Why `acks=all` prevents this

With:

```properties
acks=all
```

the leader **does not acknowledge** until:

```text
Leader        ✓
Follower1     ✓
Follower2     ✓
```

Now if the leader dies:

```text
Follower1 -> New Leader
```

Follower1 already has the record.

Nothing is lost.


### What if the leader comes back?

Suppose:

1. Leader writes Message A.
2. Leader crashes.
3. Follower1 becomes leader.
4. Old leader restarts.

The old leader's log is actually **ahead**:

```text
Old Leader
-----------
99
100  Message A
```

Current leader:

```text
New Leader
-----------
99
```

When the old leader rejoins, Kafka compares logs.

Since Message A was **never committed** (it wasn't replicated to the ISR), Kafka truncates the old leader's log:

```text
Old Leader after recovery
-------------------------
99
```

Message A is discarded to make the logs consistent.


### written vs committed

The leader already wrote the record to its local disk. Isn't that enough?
- No, not necessarily.

In Kafka, there are two different states:
1. Written to the leader's local log
2. Committed (replicated to all required ISR replicas)

- A record in state (1) is **not yet durable**.
- A record in state (2) is considered durable and safe against a single broker failure.

> Kafka guarantee save message but also High Availability of message processing
- Kafka can't guarantte High Availability of message processing with Written message. because written message is saved on specific broker.
