# Kafka message structure

- Key
  - Key is optional in the Kafka message and it can be null. 
- Value
  - The value represents the content of the message and can also be null. 
- Compression Type. 
  - Kafka messages may be compressed. The compression type can be specified as part of the message. 
  - Options are none, gzip, lz4, snappy, and zstd
- Headers
  - There can be a list of optional Kafka message headers in the form of key-value pairs. 
  - It is common to add headers to specify metadata about the message, especially for tracing.
- Partition + Offset
  - Once a message is sent into a Kafka topic, it receives a partition number and an offset id. 
  - The combination of topic+partition+offset uniquely identifies the message
- Timestamp
  - A timestamp is added either by the user or the system in the message.

### Kafka message serializers
- kafka brokers expect byte arrays as keys and values of messages.
- If you are not using a JVM-based programming language for serialization and deserialization, ensure that your Kafka client library supports the data formats that you need!
