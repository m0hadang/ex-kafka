# consumer groups CLI
- multiple kafka-console-consumer instances sharing one group and watch partition load distribute across them.

### ready
- create a topic with at least 2 partitions
```sh
podman exec kafka kafka-topics --bootstrap-server localhost:9092 --topic first_topic --create --partitions 3 --replication-factor 1
```

### consume

- run terminal 1,2,3
```sh
podman exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic first_topic --group my-first-application
```

### produce message

```sh
podman exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic first_topic
```

### produce message - RoundRobinPartitioner

```sh
podman exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic first_topic --producer-property partitioner.class=org.apache.kafka.clients.producer.RoundRobinPartitioner
```
- If you see one consumer getting all the messages, that probably means that your topic was only created with 1 partition
  - Check topic using `kafka-topics --describe` command

### gotchas
- can't use --from-beginning with --group
  - If you consume in a consumer groups using the --group command, then if you try using the --from-beginning option afterwards with the same group, it will be ignored.
- kafka-console-producer sends messages with a null key, and since Kafka 2.4 the default partitioner is the sticky partitioner (KIP-480). Instead of round-robining every message,
  - When you type messages quickly in the console producer, they all get batched to the same partition — so the same consumer keeps receiving them.
  - To Solve
    - Pause a few seconds between messages — each message ends up in its own batch, so the sticky partition rotates and different consumers pick up the messages.
    - Force round-robin partitioning
    - Send keyed messages (different keys hash to different partitions)
- If you don't specify a --group option, the consumer group of the consumer will be a random consumer

### consume behavior
1) 3 consumers, 3 partitions ==> Each consumer gets 1 partition
2) 2 consumers, 3 partitions ==> One consumer gets 2 partitions
3) 4 consumers, 3 partitions ==> One consumer sits idle
4) Consumer leaves group ==> Partitions rebalanced to remaining consumers
5) Consumer joins group ==> Rebalance distributes partitions
