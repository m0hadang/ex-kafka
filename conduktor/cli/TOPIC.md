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
```
Topic: first_topic      TopicId: 3w4VyU46Rl-8cjcfX4hGOA PartitionCount: 3       ReplicationFactor: 1    Configs: 
        Topic: first_topic      Partition: 0    Leader: 1       Replicas: 1     Isr: 1
        Topic: first_topic      Partition: 1    Leader: 1       Replicas: 1     Isr: 1
        Topic: first_topic      Partition: 2    Leader: 1       Replicas: 1     Isr: 1
```

### delete

```sh
podman exec kafka kafka-topics --bootstrap-server localhost:9092 --delete --topic first_topic
```
