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
