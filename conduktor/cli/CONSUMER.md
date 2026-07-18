# kafka-console-consumer CLI
- Read Kafka topic messages
- from-beginning flag
- partition filtering
- key and timestamp display


### consuming message - Consuming only the future messages

```sh
podman exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic first_topic
```

### consuming message - Consuming all historical messages

```sh
podman exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic first_topic --from-beginning
```

### consuming message - show both the key and value

```sh
podman exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic first_topic --formatter kafka.tools.DefaultMessageFormatter --property print.timestamp=true --property print.key=true --property print.value=true --from-beginning
```

option
```
print.partition
print.offset
print.headers
key.separator
line.separator
headers.separator
```

### properties
- `--from-beginning`
- `--formatter`
  - Display messages in a particular format
- `--consumer-property`
  - Consumer property setting. such as the allow.auto.create.topics
- `--group`
  - Consumer group ID. By default a random consumer group ID is chosen.
- `--max-messages`
  - Number of messages to consume before exiting
  - e.g., `--max-messages 3`. consume 3 message then the consumer exits automatically.
- `--partition`
  - Consume from a specific partition.

### gotchas

- If the topic does not exist, the console consumer will automatically create it with default settings
- If a consumer group id is not specified, the kafka-console-consumer generates a random consumer group
- When start a kafka-console-consumer, unless specifying the --from-beginning option, only future messages will be displayed and read
- Can consume multiple topics at a time with a comma-delimited list or a pattern