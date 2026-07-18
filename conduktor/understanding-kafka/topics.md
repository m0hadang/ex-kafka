# Kafka Topics
- Unlike DB tables, Kafka topics are not query-able. 
- By default, data in Kafka topics is deleted after one week.
  - also called the "default messag retention period" and this value is configurable.
  - This mechanism of deleting old data ensures a kafka cluster dose not run out of disk space by recycling topics over time.
- The offset is an integer value that Kafka adds to each message as it is written into a partition
  - **Each message in a given partition has a unique offset**
- Kafka topics are immutable
  - once data is written to a partition, it cannot be changed.
- Partitions are numbered starting from 0 to N-1, where N is the number of partitions.

### partition
- A topic usually consists of many partitions. These partitions are a unit of parallelism for Kafka consumers.
- Once a topic is created, you can increase the partition count but cannot decrease it.
  - partition is history of event.
  - increase the partition count meaning
    - new history will be added
  - try to decrease the partition count meaning
    - remove history(kafka message is immutable)


### why use partitions ?
- Scalability(Broker's perspective)
  - Data is distributed across multiple brokers, alowing the cluster to handle more data than a single server could
- Parallelism(Consumer's perspective)
  - Multiple consumers can read from different partitions simultaneously, increasing throughput

### what are kafka offsets ?
- represent the position of a message within a kafka partition
  - incremented for each message sent to a specific kafka partition
  - kafka offsets only have a meaning for a specific partition
- offset numbering for every partition starts at 0
- **kafka guarantees the order of messages within a partition, but there is no ordering of messages across partitions.**
- the offsets are not re-used. they continually are incremented in a never-ending sequence.

### Message ordering
- Messages with same key : Ordered (same partition)
- Messages without key : Not ordered (round-robin)
- Messages across partitions : Not ordered

# topic replication

- Kafka replication helps prevent data loss by writing the same data to more than one broker.

### replication factor
- topic setting and is specified at topioc creation time
- e.g.,
  - A replication factor of 1 : no replication.  It is mostly used for development purposes and should be avoided in test and production Kafka clusters
  - A replication factor of 3 : commonly used replication factor as it provides the right balance between broker loss and replication overhead.
- Development :	1, Saves resources, data loss acceptable
- Testing	: 2, Basic fault tolerance
- Production : 3, Industry standard, tolerates 2 failures
- Critical data : 5, Maximum durability
- `replication.factor`

### partitions leader and replicas

- leader
  - Responsible for sending and receiving data to clients.
- replica
  - Any other broker that is storing replicated data for that partition 

> Each partition has one leader and multiple replicas.

### Leader election
- process of deciding which broker is a leader at topic creation time is called a preferred leader election.

### in-sync replicas (ISR)

- An ISR is a replica that is up to date with the leader broker for a partition
  - Any replica that is not up to date with the leader is out of sync


# Kafka producers acks setting

- **Kafka producers only write data to the current leader broker for a partition.**
- acks
  - Kafka producers level of acknowledgment
  - **The message has to be written to a minimum number of replicas before being considered a successful write**
  - The default value of acks has changed in Kafka v3.0:
    - if using Kafka < v3.0, acks=1
    - if using Kafka >= v3.0, acks=all

### acks=0
- producers consider messages as "written successfully" the moment the message was sent **without waiting for the broker to accept it all**
  - if the broker goes offline or an exception happens, will lose data
- This is usefule for data where it's okay to potentially lose messages
  - e.g., metrics collection, produces the highest throughput setting because the network overhead is minimized

### acks=1
- producers consider messages as "written successfully" when the **message was acknowledged by only the leader**
- Leader response is requested, but replication is not a guarantee as it happens in the background
  - If the leader broker goes offline unexpectedly but replicas haven't replicated the data yet, we have a data loss.
- If an ack is not received, the producer may retry the request

### acks=all
- producers consider messages as "written successfully" when the message is accepted by all in-sync replicas(ISR)
  - The lead replica for a partition checks to see if there are enough in-sync replicas for safely writing the message
  - Controlled by `min.insync.replicas`
  - The request will be stored in a buffer until the leader observes that the follower replicas replicated the message, at which point a successful acknowledgement is sent back to the client
- min.insync.replicas
  - Can be configured both at the topic and the broker-level.
  - The data is considered committed when it is written to all in-sync replicas.
  - e.g., min.insync.replicas=2 
    - At least 2 brokers that are ISR (including leader) have to respond that they have the data.
  - e.g., topic has three replicas, min.insync.replicas=2
    - only write to a partition in the topoic if at least two out of the three replicas are in-sync
    - if two out of three replicas are not available, the brokers will no longer accept produce requests
      - producers that attempt to send data will receive `NotEnoughReplicasException`

### Acks comparison


Setting | Durability | Throughput | Latency | Use case
------------------------------------------------
acks=0 | None | Highest | Lowest | Metrics, logs
------------------------------------------------
acks=1 | Leader only | High | Low | Most applications
------------------------------------------------
acks=all | Full | Lower | Higher | Critical data
------------------------------------------------

# Kafka topic durability and availability

- durability
  - For a topic replication factor of 3, topic data durability can withstand the loss of 2 brokers.
    - **For a replication factor of N, you can permanently lose up to N-1 brokers and still recover your data.**
- availability
  - Reads
    - As long as one partition is up and considered an ISR, the topic will be available for reads
  - Writers
    - acks=0 & acks=1
      - as long as one partition is up and considered an ISR, the topic will be available for writes.
    - acks=all
      - min.insync.replicas=1(default)
        - the topic has to have at least 1 partition up as an ISR (that includes the reader)
          - tolerate two brokers being down
      - min.insync.replicas=2
        - the topic has to have at least 2 ISR up
          - tolerate at most one broker being down(in the case of replication factor of 3)
        - guarantee that for every write, the data will be at least written twice.
      - min.insync.replicas=3
        - couldn't tolerate any broker going down.
          - this wouldn't make much sense for a corresponding replication factor of 3
      - acks=all, replication.factor=N, min.insync.replicas=M 
        - tolerate N-M brokers going down for topic availability purposes

> Kafka topic replication settings : acks=all and min.insync.replicas=2 is the most popular option for data durability and availability and allows you to withstand at most the loss of one Kafka broker.

# Kafka consumers replicas fetching

- consumers read by default from the partition leader
  - But since Apache Kafka 2.4, it is possible to configure consumers to read from in-sync replicas instead (usually the closest).
- Reading from the closest in-sync replicas (ISR) may improve the request latency, and also decrease network costs, because in most cloud environments cross-data centers network requests incur charges.

# Preferred leader
- The preferred leader is the designated leader broker for a partition at topic creation time
- When the preferred leader goes down, any partition that is an ISR (in-sync replica) is eligible to become a new leader (but not a preferred leader).
- Upon recovering the preferred leader broker and having its partition data back in sync, the preferred leader will regain leadership for that partition.