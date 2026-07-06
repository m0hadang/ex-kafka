# kafka-topics CLI 
- list, create, describe, alter, delete topics
- set partition, replication

### create

```sh
podman exec kafka kafka-topics --bootstrap-server localhost:9092 --topic first_topic --create --partitions 3 --replication-factor 1
```

- Cannot specify a replication factor greater than the number of brokers
- No default values for partitions and replication factor
- Topic name only : ASCII alphanumerics, '.', '_', '-' 

### list

```sh
podman exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### describe

```sh
podman exec kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic first_topic
```
```sh
Topic: first_topic      TopicId: 3w4VyU46Rl-8cjcfX4hGOA PartitionCount: 3       ReplicationFactor: 1    Configs: 
        Topic: first_topic      Partition: 0    Leader: 1       Replicas: 1     Isr: 1
        Topic: first_topic      Partition: 1    Leader: 1       Replicas: 1     Isr: 1
        Topic: first_topic      Partition: 2    Leader: 1       Replicas: 1     Isr: 1
```


### increase the number of partitions

```sh
podman exec kafka kafka-topics --bootstrap-server localhost:9092 --alter --topic first_topic --partitions 5
```

- Increasing the number of partitions in a Kafka topic is a DANGEROUS OPERATION if your applications are relying on key-based ordering.
  - e.g., 2 partitions -> 4 partitions
    - 2 partitions
      - P0 : [M1, M2]
    - 4 partitions
        - P0 : [M1, M2]
        - P2 : [M3]
    - Producer order: M1 -> M2 -> M3
    - Possible consumer order: M3 -> M1 -> M2
- Increasing the partition vs Rebalancing
  - Increasing the partition count changes which partition a key allocate to.
    - there is changes in partition
  - Rebalancing moves partitions between consumers.
    - there is no changes in partition


### delete

```sh
podman exec kafka kafka-topics --bootstrap-server localhost:9092 --delete --topic first_topic
```