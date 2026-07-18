# Kafka consumers
- Applications that read data from Kafka topics are known as consumers
- Applications integrate a Kafka client library to read from Apache Kafka.
- **Consumers can read from one or more partitions at a time in kafka**, and data in read in order within each partition
- A consumer always reads data from a lower offset to a higher offset and cannot read data backwards
- if the consumer consumes data from more than one partition, the message order is not guaranteed across multiple partitions
  - they are consumed simultaneously
- **By default, kafka consumers will only consume data that was produced after it first connected to Kafka**


### Consumer pull model
- Kafka consumers have to request data from Kafka brokers in order to get it 
  - (instead of having Kafka brokers continuously push data to consumers)
- This implementation was made so that consumers can control the speed at which the topicsd are being consumed
- Benefits of the pull model:
  - **Consumers control their own consumption rate**
  - Slow consumers don't affect broker performance
  - Consumers can batch process messages efficiently
  - Natural backpressure handling

### Serialization compatibility
- The serialization and deserialization format of a topic should not change during a topic lifecycle. If you intend to switch a topic data format (for example from JSON to Avro), it is considered best practice to create a new topic and migrate your applications to leverage that new topic.
