# Kafka KRaft mode

- The metadata of Kafka itself is a log and that Kafka brokers should be able to consume that metadata log as an internal metadata topic. 
  - Kafka leverages itself!
  - It has been noted as part of KIP-500 that 
- KRaft was officially released as production ready in Kafka version 3.3. 
- Benefit
  - Scale:	Ability to scale to millions of partitions
  - Simplicity:	Single process to start Kafka, easier to maintain and set up
  - Stability:	Improved stability, easier to monitor, support, and administer
  - Security:	Single security model for the whole system
  - Performance:	Faster controller shutdown and recovery time

### KRaft deployment modes
- Combined mode
  - Controllers and brokers run in the same process.
    - Development environments
    - Small clusters (3-5 nodes)
    - Simplified operations
  - `process.roles=broker,controller`
- Isolated mode
  - Controllers and brokers run as separate processes.
    - Production environments
    - Large clusters
    - Maximum stability
```
# On controller nodes
process.roles=controller

# On broker nodes
process.roles=broker
```

