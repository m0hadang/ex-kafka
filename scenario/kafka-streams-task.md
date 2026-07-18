# Task

> definition

- fundamental unit of work in Kafka Streams.
  - It is not the same as a Kafka consumer or a thread.
- definition of kafka streams logical job
  - join each partitions from different topics
- task owns:
  - One partition from each input topic
  - The processor topology for those partitions
  - Any local state stores for those partitions

- Task = "Run this topology for these partitions."



### Example

two topics:

```
Orders:     4 partitions
Customers:  4 partitions
```

topology:

```java
orders.join(customers)
```

kafka streams creates four tasks

```
Task 0
  Orders-0
  Customers-0

Task 1
  Orders-1
  Customers-1

Task 2
  Orders-2
  Customers-2

Task 3
  Orders-3
  Customers-3
```

Each task independently executes the join for its assigned partitions




### Tasks are assigned to threads
- Each thread executes its assigned tasks one at a time
  - task is not a thread. it's a piece of work that a thread runs.

```
KafkaStreams Instance

Thread-1
    Task 0
    Task 1

Thread-2
    Task 2
    Task 3
```

### Multiple instances

```
Instance A

    Thread 1
    
        Task 0
          Orders-0
          Customers-0
          
        Task 1
          Orders-1
          Customers-1

Instance B

    Thread 1
    
        Task 2
          Orders-2
          Customers-2
          
        Task 3
          Orders-3
          Customers-3
        
```
  
If Instance A crashes, Kafka Streams performs a rebalance and may reassign tasks:

```
Instance B

    Thread 1
    
        Task 0
          Orders-0
          Customers-0
          
        Task 1
          Orders-1
          Customers-1

        Task 2
          Orders-2
          Customers-2
          
        Task 3
          Orders-3
          Customers-3
   
```

> The tasks move between instances, but their definition (which partitions they own) does not change.

### Task structure

```
# join
orders.join(customers)

# structure
Task 2

Input:
    Orders-2
    Customers-2

State Store:
    Customers Store
    Orders Store (if needed)

Processor:
    JoinProcessor

Output:
    Joined Topic

# Every record from Orders-2 and Customers-2 is processed by this task.    
```

### Stream Thread is the kafka consumer

```
Kafka Streams Application
│
├── Instance 1 (JVM)
│   │
│   ├── Stream Thread 1
│   │      ├── Task 0
│   │      └── Task 1
│   │
│   └── Stream Thread 2
│          └── Task 2
│
└── Instance 2 (JVM)
    │
    └── Stream Thread 1
           └── Task 3
```

- Each stream thread owns a Kafka consumer that fetches records from its assigned partitions.
  - Thread: Executes one or more tasks.
- Task
  - Processes records for a fixed set of partitions
    - one corresponding partitions from each source topic in the topology
  - maintains any local state, and runs the processor topology
- "joins happen within a task" means
  - task owns the relevant partitions(e.g., Orders-2 and Customers-2)
  - processes them together using its local state stores

# Why are tasks important?

> kafka streams guarantees that all records for a given key arrive at the same **task**


- Both records go to Task 2, so the join can be performed entirely within that task without communicating with other tasks.

```
key = "user123"

Orders:
hash(user123) % 4 = 2

Customers:
hash(user123) % 4 = 2
```
