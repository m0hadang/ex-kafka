### stream processing
- process the received data immediately and reponse
- ex) Apache Spark Streaming, Apache Flink
- attribute
  - defect tolerance
  - data stream convert operation
  - advanced stream expression
  - elaborate time processing
- function
  - agregate record
  - stream join
  - window bucket
  - event grouping
  - stream query
  - ...
- easy to scale out
  - copy application instance or thread

### processing model
- micro match
  - memroy buffering
  - grouping record
  - process at regular intervals
  - ex) apache spark streaming, trident
- event at a time
  - process event immediately, one at a time, as soon as they arrive.
    - kafka
  - low latency
  - ex) kafka streams

### DAG(Directed Acyclic Graph)
- stream logic is built as DAG(none cycle graph)

### processor topology
- component
  - source processor
    - receive record and forward to stream processor
  - stream processor
    - data processing/logic
    - filter, map, flatMap, join, ...
  - sink processor
    - produce record to kafka
- only one record passes through the topology at a time
  - depth first 
  - protect other record processing in same thread
- parallel template using thread, application instance
  - dosen't run, but just make defined template how data should be flow
- init several time in single application by multiple stream threads and tasks


### sub-topology
- independantly runable topology
- data repartitioning possible
  - ex) receive data from one topic, and split two topic that handled by sub-topology

### stream task
- minimum unit of parallelable job in kafka stream application
  - maximum of parallel count is limited by maximum of stream task count
    - Can work in parallel as many times as the number of stream tasks
  - maximum of stream task count is determined by max input topic partition count
- maximum stream task count in sub-topology
  - max(src_topic_1_partion_cnt, src_topic_2_partion_cnt, ...)
  - how to count ?
    - sub-topology(src_topic_1)
      - sub-topology(src_topic_2)
        - sub-topology(...)
          - ...
      - ...
    - must calculate the number of all sub-topologies configured below.

### instantiate topology
- if there is topology that receive source topic(16 partition)
  - create 16 stream task
  - each stream task instantiate a topology  
  - assign source partition to each stream task
- stream task is the logical unit that instantiate a topology and run

### stream thread
- actually run stream task
- in kafka streams, threads are independent execution units, and each task/state store is owned by one thread at a time.
- num.stream.threads
  - unlike stream task, number of stream thread is determined by "num.stream.threads" option
  - strategy
    - ex) cpu 4 core, 16 stream tasks
      - stream thread: 4
        - 4 core x 1 stream thread = 4 stream thread
        - 4 stream thread x 4 stream tasks = 16 stream tasks
        - `==> 1 core { 1 stream thread { 4 stream tasks`
      - stream thread: 8
        - 4 core x 2 stream thread = 8 stream thread
        - 8 stream thread x 2 stream tasks = 16 stream tasks
        - `==> 1 core { 2 stream thread { 2 stream tasks`
    - ex) cpu 48 core, 16 stream tasks
      - stream thread: 16
        - 16 stream thread x 1 stream tasks      
        - `==> 1 core { 1 stream thread { 1 stream tasks`
- (effective active stream thread count) <= (active stream task count)
  - Not a hard rule. You can configure more threads than tasks; extra threads just sit idle.


### API
- DSL - High Level API
  - functional programming
  - abstraction
- Processor API - Low Level API
  - method
  - must implement stream processor using Processor interface
    - init, process, close

### DLS
- DLS produce data modeling view for topic
  - stream(record stream)
  - table(changelog stream)
- stream
  - act like DB insert
  - remain all data
- table
  - act like DB update
  - remain last data
  - essentially, a table is stateful
  - "almost use for aggregate data"
    - ex) counting record count
  - almost use for compacted topic(cleanup.policy=compact)
  - state store
    - client key-value stroe
      - consume event and save last record to "client" key-value store
    - many implementaions
      - RocksDB(embeding key, value store )
      - in memory

### stream, table
- stream and table can convert each other
  - ex) mysql, redis, ...
  - redis build dataset using AOF



### KStream, KTable, GlobalKTable
- KStream
  - view of partitioning record stream
  - insert semantic
- KTable
  - view of partitioning table
  - update semantic
- GlobalKTable
  - all copied data