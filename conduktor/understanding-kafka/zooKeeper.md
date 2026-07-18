# ZooKeeper with Kafka

### ZooKeeper is being eliminated from Kafka

- Kafka 0.x, 1.x & 2.x must use ZooKeeper
- Kafka 3.x can work without ZooKeeper (KRaft mode) and is production ready as of 3.3
- Kafka 4.x will not have ZooKeeper

### ZooKeeper in Kafka
- ZooKeeper keeps track of which brokers are part of the Kafka cluster
- ZooKeeper is used by Kafka brokers to determine which broker is the leader of a given partition and topic and perform leader elections
- ZooKeeper stores configurations for topics and permissions
- ZooKeeper sends notifications to Kafka in case of changes (e.g. new topic, broker dies, broker comes up, delete topics, etc.)
- Consumer offsets: ZooKeeper does NOT store consumer offsets with Kafka clients >= v0.10. Offsets are stored in the internal __consumer_offsets topic.

