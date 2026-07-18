# increase-task-count
- Can't incrase task count directly
  - Unlike threads, tasks are not configurable.
- number of tasks is determined by the number of input topic partitions.

```
Number of input partitions
        ↓
Number of tasks
        ↓
Tasks are assigned to application instances/threads
```

```
orders
- P0
- P1
- P2
- P3

Kafka Streams creates 4 tasks:
- Task 0 ← P0
- Task 1 ← P1
- Task 2 ← P2
- Task 3 ← P3
```

# Get more task

### Increase the input topic's partition count

```
Increase the topic to 8 partitions:

8 partitions
↓
8 tasks
```

### Repartition into a topic with more partitions
- This is the preferred approach when you need more parallelism within a topology
  - because all records are rewritten consistently according to the new partitioning.
```
Kafka Streams creates an internal repartition topic with 16 partitions.

stream
    .selectKey(...)
    .repartition(Repartitioned.numberOfPartitions(16))

16 partitions
↓
16 tasks    
```

### Adding more application instances -> no

Initially:

```
Instance A

Task 0
Task 1
Task 2
Task 3
Task 4
Task 5
Task 6
Task 7
```

Start another instance:

```
Instance A
Task 0
Task 1
Task 2
Task 3

Instance B
Task 4
Task 5
Task 6
Task 7
```

- The number of tasks is still 8. They are simply redistributed among the instances.

### num.stream.threads -> no

- increasing the number of threads allows more tasks to **run concurrently**, but it does not create additional tasks.

# Rule of thumb

- A Kafka Streams task is fundamentally tied to input partitions. 
- If you need more tasks, you need more input partitions—either by increasing the partition count of the input topic (with the caveats for stateful processing) or, more commonly within a topology, by repartitioning into a topic with a larger number of partitions.
