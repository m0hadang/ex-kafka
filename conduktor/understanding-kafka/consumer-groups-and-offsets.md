# Kafka consumer groups and offsets
- horizontal scalabilit
  - The benefit of leveraging a Kafka consumer group is that the consumers within the group will coordinate to split the work of reading from different partitions.
- Consumers that are part of the same application and therefore performing the same "logical job" can be grouped together as a Kafka consumer group.

### Kafka consumer group ID
- group.id
  - consumer-side setting
  - indicating to kafka consumers that they are part of the same specific group
- consumers automatically use a GroupCoordinator and a ConsumerCoordinator to assign consumers to a partition and ensure the load balancing is achieved across all consumers in the same group
- **each topic partition is only assigned to one consumer within a consumer group**
  - but a consumer from a consumer group can be assigned multiple partitions
  - partition messages must be processed in order
- multiple consumer groups can consume from the same topioc at the same time

### Consumer scaling limits
- **If there are more consumers than the number of partitions of a topic, then some consumers will remain inactive**
  - If want more consumers for higher throughput, should create more partitions while creating the topic. Otherwise, some of the consumers may remain inactive.
- Maximum parallelism
  - The maximum number of active consumers in a group equals the number of partitions
- e.g, 
  - Partitions, Consumers : Result
  - 3, 1 : 1 consumer handles all 3 partitions
  - 3, 2 : Each consumer handles 1-2 partitions
  - 3, 3 : Each consumer handles 1 partition (optimal)
  - 3, 5 : 2 consumers idle, 3 active

### Kafka consumer offsets
- `__consumer_offsets`
  - Kafka brokers internal topic
  - keeps track of what messages a given consumer group last successfully processed.
- in order to **checkpoint** how far a consumer has been reading into a topic partition, the consumer will regularly **commit** the latest processed message
  - => consumer offset
- **Most client libraries automatically commit offset to kafka(on a periodic basis) and responsible kafka broker will ensure writing to the __consumer_offsets topic**
  - (therefore consumers do not write to that topic directly)
- **periodic committing offsets**
  - **The process of committing offsets is not done for every message consumed, because this would be inefficient**
  - **specific offset is committed, all previous messages that have a lower offset are also considered to be committed**
- Why use consumer offsets?
  - kafka client crashes, a rebalance occurs and the latest committed offset help the remaining kafka consumers know where to restart reading and processing messages.
  - e.g, : In case a new consumer is added to a group, another consumer group rebalance happens and consumer offsets are yet again leveraged to notify consumers where to start reading data from.

### Delivery semantics for consumers
- enable.auto.commit=true
- auto.commit.interval.ms
  - default: 5 seconds
  - **Java consumers automatically commit offsets every `auto.commit.interval.ms` when .poll() is called.**
- A consumer may opt to commit offsets by itself (enable.auto.commit=false). 
  - Depending on when it chooses to commit offsets, there are delivery semantics available to the consumer

### At most once
- Offsets are committed as soon as the message is received
- If the processing goes wrong, the message will be lost
  - it' won't be read again: because offset is alread commited

### At least once(usually preferred)
- Offsets are committed after the message is processed
- If the processing goes wrong, the message will be read again
- This can result in duplicate processing of messages. Therefor, it is best practice to make sure data processing is idempotent

### Exactly once
- **This can only be achieved for kafka topic to kafka topoic workflows using the transactinos API**
- The kafka streams api simplifies the usage of the API and eanbles exactly once using the setting
  - **processing.guarantee=exactly_once_v2 (exactly_once on Kafka < 2.5)**
- For topic to external system workflows, to effectively achieve exactly once, you have to use an idempotent consumer
- Recommended approach
  - In practice, at least once with idempotent processing is the most desirable and widely implemented mechanism for Kafka consumers.

### semantics

- At most once:	
  - Commits when: Before processing	
  - Rist: Data loss	
  - Use case: Metrics, logs
- At least once:	
  - Commits when: After processing	
  - Rist: Duplicates	
  - Use case: Most applications
- Exactly once:	
  - Commits when: With transaction
  - Rist: Complexity
  - Use case: Financial, critical