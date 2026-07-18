# kafka-console-producer CLI
- send messages 
- acks configuration
- delivery verification

### ready
```sh
podman exec kafka kafka-topics --bootstrap-server localhost:9092 --topic first_topic --create --partitions 3 --replication-factor 1
```

> A topic should be existed before produce message in operation environment

- If the topic does not exist, it can be auto-created by Kafka
- A new topic will be created with the default number of partitions and replication factor.

config/server.properties
```
auto.create.topics.enable=true
num.partitions=1
default.replication.factor=1
```

### produce message - interactive mode
- Messages are sent with the null key by default

```sh
podman exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic first_topic
```
- from file : `kafka-console-producer --bootstrap-server localhost:9092 --topic first_topic < topic-input.txt`

### produce message with key
```sh
podman exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic first_topic --property parse.key=true --property key.separator=:
```

Example input:
```
>example key:example value
>name:John
```

### producer argument 

- `--compression-codec`
  - To enable message compression, default gzip, possible values 'none', 'gzip', 'snappy', 'lz4', or 'zstd'
- `--producer-property`
  - producer property setting(e.g., acks=all, etc...)
- `--request-required-acks`
  - An alternative to set the acks setting directly

| Value           | Meaning                                             | Durability | Performance |
| --------------- | --------------------------------------------------- | ---------- | ----------- |
| `0`             | Producer does not wait for any acknowledgment.      | Lowest     | Fastest     |
| `1`             | Wait for the leader replica to acknowledge.         | Medium     | Fast        |
| `-1` (or `all`) | Wait for all in-sync replicas (ISR) to acknowledge. | Highest    | Slowest     |


```
--request-required-acks 0
Producer
    |
    | Send
    v
Broker
```
- No confirmation.
- **If the broker crashes immediately, the producer may never know the record was lost.**

```
--request-required-acks 1
Producer
    |
    | Send
    v
Leader Replica
    |
    | ACK
    v
Producer
```
- The producer waits until the leader writes the record.
- **If the leader crashes before followers replicate the record, the record can still be lost.**


```
--request-required-acks -1 (or all)
Producer
      |
      | Send
      v
Leader
  |
  +--> Follower1
  |
  +--> Follower2
```
- **The producer receives an acknowledgment only after all in-sync replicas (ISR) have stored the record.**


> If you enable idempotence or transactions, Kafka requires `acks=all` properties

```
enable.idempotence=true
```

- idempotence and transactions rely on the record being durably replicated before the producer considers it successful.
