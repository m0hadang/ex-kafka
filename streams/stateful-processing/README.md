### operation
- join : enrich data with another stream to table
  - join(inner join)
  - leftJoin
  - outherJoin
- aggregate : mathematical/combinatorial transformation calculation
  - aggregate
  - count
  - reduce
- windowing : grouping events that have temporal proximity
  - windowedBy

### state store
- for stateful processing, state store is required.
- kafka streams support various state store implementation and configuration
- embeding
  - state store is embedded to kafka streams application task
    - no need to network call
    - no single point of failure
  - embedded to only one task
    - no concurrency problem
  - using RocksDB
    - embeded key value store
    - support save byte stream
- Multiple access modes
- tolerance
  - state store backup state using kafka change log
    - can be turned off
  - play change log and restore state store
  - can reduce restore time with standby replica
- key value based
  - record key define relation of events
  - internal data structure is various based on state store type
    - persistent : RocksDB
    - in-memory : java TreeMap
  - sometime key is complex
    - Windows Store : key has windows time

### state store types
- persistent(RocksDB)
  - flush data to disk asynchronously
  - can use more memory by store memory to disk
  - qucik recovery possible rather then in-memory
    - no need to play entire topic, instead just replay missing data while on application down time
  - It is complex in terms of operation and may be slow in performance.
    - ex) disk issue, RocksDB configuration, ...
- in-memory
  - it is more time to disaster recovery
    - replay all topic
  - Start with a persistent at first, and then 
    - switch to in-memory storage only when 
      - performance improvement is clear
      - fast recovery using stand by replica to reduce recovery time.

### KTable vs GlobalKTable
- KTable
  - if key space is big, use KTable
    - high cardinality(many unique key)
  - split state and can be distributed among instances of each running application
    - reduce local state store overhead
  - When deciding what record to process next, look at the time.
- GlobalKTable
  - low cardinality
  - When you need to replicate state to the entire application instance, use GlobalKTable
  - It is not time-synchronized and must be fully filled before any processing is performed.
  - When you want to avoid co-partitioning

### co-partitioning
- each partition is allocated to single kafka streams task
- what if join ?
  - when aggregating a series of events, it must be ensured that related events are routed to the same partition and processed in the same task.
  - => co-partitioning
- related events are routed to the same partition
  - both records should use same field as key and has same partitioning strategy, partitioning to key
    - handle with selectKey
  - joined topics must have same partition count
    - this is first check condition when kafka application start(TopologyBuilderException)

### internal topic
- while on processing streams, new internal topic is created automatically
- repartitioning
  - selectKey, ...
- join
  - changelog topic for restore state store
  - join also create state store 

### ref
- https://www.buzzsprout.com/186154/episodes/2555848-streaming-call-of-duty-at-activision-with-apache-kafka-ft-yaroslav-tkachenko
