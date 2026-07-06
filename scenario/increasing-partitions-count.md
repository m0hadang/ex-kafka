# increase the number of partitions

- Increasing the number of partitions in a Kafka topic is a DANGEROUS OPERATION if your applications are relying on key-based ordering.

```sh
podman exec kafka kafka-topics --bootstrap-server localhost:9092 --alter --topic first_topic --partitions 5
```

### e.g., 2 partitions -> 4 partitions
- e.g., 2 partitions -> 4 partitions
  - 2 partitions
    - P0 : [M1, M2]
  - 4 partitions
      - P0 : [M1, M2]
      - P2 : [M3]
  - Producer order: M1 -> M2 -> M3
  - Possible consumer order: M3 -> M1 -> M2

### Increasing the partition vs Rebalancing
- Increasing the partition count changes which partition a key allocate to.
  - there is changes in partition
- Rebalancing moves partitions between consumers.
  - there is no changes in partition
