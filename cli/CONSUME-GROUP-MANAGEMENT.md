# consumer group management CLI
- list groups,
- inspect consumer lag with describe
- reset offsets 
- delete stale groups

### consumer group info

```
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-first-application

GROUP                TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                                          HOST            CLIENT-ID
my-first-application first_topic     0          3               3               0               consumer-my-first-application-1-0237b0a1-911d-45f1-891f-8fd7630a7593 /172.19.0.1     consumer-my-first-application-1
my-first-application first_topic     1          5               5               0               consumer-my-first-application-1-70ddc756-8dbc-45e4-b5b6-1e5a75db9e62 /172.19.0.1     consumer-my-first-application-1
my-first-application first_topic     2          6               6               0               consumer-my-first-application-1-a8ce2af3-97b3-4445-bba3-f4a5f6c4464d /172.19.0.1     consumer-my-first-application-1
```
- CONSUMER ID
  - unique identifier of the consumer to the Kafka broker
  - must be unique within a consumer group
- CLIENT ID
  - `client.id` consumer property
  - client-side setting that can optionally set to identify(description) a consumer in consumer groups
  - client.id is not intended to uniquely identify a consumer instance.
    - It is mainly used for logging, metrics (JMX), request tracing, quota management
- CURRENT-OFFSET
  - Latest committed offset for that group
  - Next offset the consumer will read if it restarts.
- LOG-END-OFFSET
  - Latest message offset available in the topic-partition for consumption
  - The offset of the next message that would be appended to the partition.
- LAG
  - LOG-END-OFFSET - CURRENT-OFFSET
  - represents how far behind a consumer is to the tail of a topic.
- HOST
  - hostname / IP of the consumer client machine.

```
[partition messages]
Offset    Message
------    -------
0         A
1         B
2         C
3         D
4         E

[consumer has processed]
A
B
C

CURRENT-OFFSET = 3
LOG-END-OFFSET = 5
- The next produced message would receive: "Offset = 5"
```

### check consumer group status
- Cannot reset a consumer group if consumers are active in it.
- First, ensure that the consumers are stopped ("has no active members")
```
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-first-application

Consumer group 'my-first-application' has no active members.

GROUP                TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
my-first-application first_topic     0          3               3               0               -               -               -
my-first-application first_topic     1          5               5               0               -               -               -
my-first-application first_topic     2          6               6               0      
```

- check all consumer
```sh
podman exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-first-application --members

GROUP                CONSUMER-ID                                           HOST            CLIENT-ID        #PARTITIONS     
my-first-application console-consumer-ff61f66b-dc11-48b3-85af-ef8e6e253375 /127.0.0.1      console-consumer 3    
```

- check consumer group state
```sh
podman exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-first-application --state               

GROUP                     COORDINATOR (ID)          ASSIGNMENT-STRATEGY  STATE           #MEMBERS
my-first-application      localhost:9092 (1)        range                Stable          1
```

### Reset offsets to the earliest
```
kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --to-earliest --execute --topic first_topic

GROUP                          TOPIC                          PARTITION  NEW-OFFSET
my-first-application           first_topic                    0          0
my-first-application           first_topic                    1          0
my-first-application           first_topic                    2          0
```

-  new offsets for that consumer group for all partitions are 0, which means that upon restarting a consumer in that group, it will read from the beginning of each partition:
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic first_topic --group my-first-application
third message
fifth message
seventh message
tenth message
first message
fourth message
eigth message
hello
world
second message
sixth message
ninth message
```

### Reset offsets shift by

```
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-first-application

Consumer group 'my-first-application' has no active members.
GROUP                TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
my-first-application first_topic     0          3               3               0               -               -               -
my-first-application first_topic     1          5               5               0               -               -               -
my-first-application first_topic     2          6               6               0               -               -               -
```

- reset offsets by shifting by -2
```
kafka-consumer-groups --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --shift-by -2 --execute --topic first_topic

GROUP                          TOPIC                          PARTITION  NEW-OFFSET
my-first-application           first_topic                    0          1
my-first-application           first_topic                    1          3
my-first-application           first_topic                    2          4
```

- last 2 messages from each partition of the topic.
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic first_topic --group my-first-application
seventh message
tenth message
fourth message
eigth message
sixth message
ninth message
```

### Listing consumer groups state

```sh
podman exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list --state
```
```
GROUP                STATE
my-first-application Stable
```

### Describe all consumer groups and state

- helpful for assignment strategy and coordinator ID
```sh
podman exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --all-groups  --state
```
```
GROUP                     COORDINATOR (ID)          ASSIGNMENT-STRATEGY  STATE           #MEMBERS
my-first-application      localhost:9092 (1)        range                Stable          1
```

# Delete consumer group
```sh
podman exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group my-first-application
```

# Delete consumer group offset
```sh
podman exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete-offsets --group my-first-application --topic first_topic
```

### properties
- `--all-groups`
  - Applies to all groups, use with caution
- `--all-topics`
  - Consider all topics assigned to a group in the reset-offsets process, use with caution
- `--by-duration`
  - Reset to offsets by duration
- `--dry-run`
- `--to-datetime, --by-period, --to-earliest, --to-latest, --shift-by, --from-file, --to-current`
  - All the various options available to you to reset the offsets

### Gotchas
- This command can be used to reprocess data for a consumer group
  - in case you have a bug fix
- This command be also be used to advance message consumption in Kafka
  - for example if a message is a poison pill, or if your consumer is too slow to catch up with the entire topic
