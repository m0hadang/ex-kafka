# Kafka producers
- Applications typically integrate a Kafka client library to write to Apache Kafka
- In case the key (key=null) is not specified by the producer, messages are distributed evenly across partitions in a topic.
  - message are sent in a round-robin fasion
- **If a key is sent (key != null), then all messages that share the same key will always be sent and stored in the same Kafka partition**
- message keys are commonly used when there is a need for message ordering for all messages sharing the same field
  - use message key when you need ordering guarantees for related messages
  - skip the key when maximum throughput is more important than ordering

### Kafka message key hashing
- A Kafka partitioner is a code logic that takes a record and determines to which partition to send it into.
  - Conceptually: `partition = hash(key) % numberOfPartitions`
  - **The partitioner is inside the Kafka Producer client, not on the broker.**
  - **Kafka key hashing is the process of determining the mapping of a key to a partition.**
- default Kafka partitioner
  - keys are hashed using the murmur2 algorithm
  - targetPartition = Math.abs(Utils.murmur2(keyBytes)) % (numPartitions - 1)
- **increase the number of partitions for a topic, the same key may hash to a different partition. This breaks ordering guarantees for existing keys.**
- You can override the default partitioner via the producer property partitioner.class, although it is not advisable unless you know what you are doing.
