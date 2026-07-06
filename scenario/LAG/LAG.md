# Troubleshoot consumer lag
- Consumer lag occurs when consumers can't keep up with the rate of incoming messages
- LAG = LOG-END-OFFSET - CURRENT-OFFSET
  - LAG = 0: Consumer is fully caught up
  - LAG > 0 and stable: Consumer is behind but processing consistently
  - LAG > 0 and growing: Consumer is falling further behind (problem!)

## Check consumer lag

```
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group my-consumer-group

GROUP            TOPIC      PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
my-consumer-group orders    0          1500            2500            1000
my-consumer-group orders    1          2300            2350            50
my-consumer-group orders    2          900             5000            4100
```
- Partition 0: 1,000 messages behind
- Partition 1: 50 messages behind (acceptable)
- Partition 2: 4,100 messages behind (critical!)

# Common causes and solutions

## Slow message processing

- Symptoms
  - LAG increases steadily over time
  - Consumer CPU usage is high
  - Processing time per message is high

- Diagnosis
```
# Check if lag is growing
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group my-consumer-group

# Run again after 60 seconds and compare LAG values
```

- Solution
  - Optimize consumer code
    - reduce processing time per message
  - Add more consumer instances(up to number of partitions)
  - Increase consumer threads(num.streams in older API)
  - Use async processing where possible

## Under-partitioned topic

- Symptoms
  - Maximum consumers reached but stll have lag
  - Cannot add more consumers to scale
  - Single partition has very high lag
- Diagnosis
```
# Check partition count
kafka-topics --bootstrap-server localhost:9092 \
  --describe --topic orders

Topic: orders  PartitionCount: 3  # Only 3 partitions!
```

- Solution
  - Increase partition count(careful: can't decrease later)
  - Create new topic with more partitions and migrate
  - Consider if messages can be processed in parallel

## Network or broker issues

- Symptoms
  - Sudden spike in lag across all consumers
  - Intermittent connection errors
  - Broker CPU or disk I/O is saturated

- Diagnosis
```
# Check broker health
kafka-broker-api-versions --bootstrap-server localhost:9092

# Monitor broker metrics (requires JMX)
# Check: CPU, disk I/O, network throughput
```

#### Solution
- Check network connectivity between consumers and brokers
- Add more brokers if cluster is overloaded
- Optimize broker configuration(buffer sizes, threads)
- Check disk performance(especially if using spinning disks)


## Consumer rebalancing

- Symptoms
  - Periodic spikes in lag
  - LAG increases then decreases repeatedly
  - Consumer logs show "Revoke" and "Assign" messages
- Diagnosis
```
# Check consumer logs for rebalancing events:

[Consumer] Revoking previously assigned partitions
[Consumer] partitions lost: [orders-0, orders-1]
```

```
[rebalancing]
Consumer A
   Orders-0
   Orders-1
↓
Rebalance
↓
Consumer B
   Orders-0
Consumer C
   Orders-1

[lag status]
lag ↑
rebalance
lag ↓

lag ↑
rebalance
lag ↓
```

```
# Why does this reduce lag spikes?
Stop polling
↓
Revoke partitions
↓
Assign partitions
↓
Restore state (Kafka Streams)
↓
Resume polling

# During that time:
# - producers continue writing
# - nobody consumes
# - So lag grows.
```

#### Solution - Use incremental cooperative rebalancing
- Kafka consumer rebalancing protocol (introduced in Kafka 2.4, KIP-429) that lets consumers keep processing the partitions they already own during a rebalance, instead of everything stopping.

#### Solution - Increase session.timeout.ms(default: 10s ->45s)
- can reduce unnecessary rebalances 
- group coordinator uses `session.timeout.ms` to decide whether a consumer is dead.
  - Every consumer periodically sends a heartbeat to the group coordinator.
  - `Consumer  --------Heartbeat-------> Coordinator`
- because of some temporary issues, Consumer reply Heartbeat reply within `session.timeout.ms`
  - JVM pauses because of GC.
  - CPU is overloaded.
  - Network hiccups.
  - Kubernetes pauses the container briefly.
  - etc...
- `session.timeout.ms` typical values

| Workload                  | session.timeout.ms |
| ------------------------- | -----------------: |
| Stable data center        |            10–15 s |
| Kubernetes                |            30–45 s |
| Occasional long GC pauses |            30–60 s |
| WAN / unreliable network  |   Sometimes higher |


#### Solution - Increase max.poll.interval.ms if processing takes long
- If the consumer is alive but spends a long time processing records without polling.
  - If processing exceeds max.poll.interval.ms, Kafka considers the consumer stuck and removes it from the group, triggering a rebalance.
- Not heartbeats
  - it's that the consumer isn't calling poll() frequently enough. 

not heartbeats code
```java
while (true) {
    ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(100));
    processFor20Minutes(records);   // very slow
}
```


#### Solution - Enable static group membership for planned restarts
- `group.instance.id=<unique-instance-id>`
- Especially when restart consumers
  - e.g., deployments, rolling updates, or maintenance
- This feature is called Static Group Membership, introduced by KIP-345.
- Each consumer instance must have a unique group.instance.id.

[Dynamic Membership]
- Normally, consumers join a group dynamically.

```
# three consumers:
Consumer A
Consumer B
Consumer C

# Kafka assigns:
P0 -> A
P1 -> B
P2 -> C

# restart Consumer B.
Consumer B stops

# The group coordinator sees that a member has left.
# It immediately starts a rebalance:
P0 -> A
P1 -> A
P2 -> C

P2 -> C

# A few seconds later, Consumer B comes back.
# Kafka treats it as a "brand new consumer"(Dynamic Membership) because it gets a new member ID.
# Another rebalance occurs:
P0 -> A
P1 -> B
P2 -> C

```
- single restart causes two rebalances:
  - When B leaves.
  - When B rejoins.

[Static Group Membership]

```
# configure properties
# These IDs are stable across restarts.
Consumer A
group.instance.id=consumer-a

Consumer B
group.instance.id=consumer-b

Consumer C
group.instance.id=consumer-c


# restart Consumer B.
Consumer B stops
↓
Consumer B starts again
↓
group.instance.id = consumer-b
```

- The coordinator recognizes: "This is the same logical consumer that owned partition P1."
  - If it comes back before the session expires, Kafka can keep its partition assignment without treating it as a completely new group member, avoiding the extra rebalance.

- *This greatly reduces partition movement during planned restarts.*
  - e.g., 
    - Kubernetes rolling deployments
    - Planned application restarts
    - VM or server reboots
    - Kafka Streams applications
    - Stable, long-running consumer instances

# Static Group Membership and `session.timeout.ms`

These two settings work well together.

```
session.timeout.ms = 45s
group.instance.id = consumer-b
```

```
0s
Consumer B stops

20s
Consumer B starts

Coordinator:
"This is consumer-b again."
```
- No unnecessary reassignment.

```
# If Consumer B returns before the session timeout expires, it resumes as the same group member.
45s

Coordinator:
"consumer-b is gone."

↓

Rebalance
```
- still recover from genuine failures.



# Best practices
Monitor lag continuously:
- **Track lag growth rate, not just absolute value**
- **Monitor per-partition lag, not just group average**
- Set up alerts for LAG > threshold (e.g., 1000 messages)

Prevent lag:
- **Start with enough partitions**
  - 2-3x expected consumers
- **Use incremental rebalancing to minimize disruption**
- Optimize consumer processing before adding instances
- Test consumer performance under load before production
