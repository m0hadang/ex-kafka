# ex-kafka

### unit of task in kafka
- **topic partition**
  - if there is 32 source topic partition, 
  - "32 source topic partition" == "32 distributable tasks"


### recommended pattern for increasing partitions
1. create new source topic with the desired number of partitions
2. Migrate all existing workloads to a new source topic
