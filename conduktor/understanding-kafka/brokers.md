# Kafka brokers
- storing partitions
- electing leaders
- serving requests via bootstrap servers
- A single Kafka server is called a kafka broker
  - runs on the JVM
- responsibilities 
  - Store data for topic partitions on disk
  - Handle read and write requests from producers and consumers
  - Manage partition replication
  - Coordinate with other brokers in the cluster

# Kafka brokers and topics
- brokers store data in directory on the server disk they run on
- Each topoic-partition receives its own sub-directory with the associated name of the topic
- kafka does a good job of distributing partitions evenly among the available brokers
- In case the cluster becomes unbalanced due to an overload of a specific broker, it is possible for kafka administratos to rebalance the cluster and move partitions
- Partition limits
  - Each broker has practical limits on the number of partitions it can handle (typically 2,000-4,000). 
  - Monitor partition counts as your cluster grows.

# Clients connect to a kafka cluster(bootstrap server)
- Any broker in the cluster is also called a bootstrap server.
- The bootstrap server will return metadata to the client that consists of a list of all the brokers in the cluster
- The client will know which exact broker to connect to to send or receive data, and accurately find which brokers contain the relevant topic-partition.
- In practice, it is common for the Kafka client to reference at least two bootstrap servers in its connection URL
  - In the case one of them not being available, the other one should still respond to the connection request.

> Kafka clients (and developers/DevOps) do not need to be aware of every single hostname of every single broker in a Kafka cluster, but only to be aware and reference two or three in the connection string for clients.

```
# Example bootstrap server configuration
bootstrap.servers=broker1.example.com:9092,broker2.example.com:9092
```

