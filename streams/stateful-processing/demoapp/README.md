
### TODO

- https://www.conduktor.io/glossary/state-stores-in-kafka-streams#stream-joins
  - Stream Joins and Enrichment Patterns
    - https://www.conduktor.io/glossary/stream-joins-and-enrichment-patterns




### UP

```sh
podman compose up -d
```

- Kafka broker (port 9092)
- Kafka UI (port 8080)
- Schema Registry (port 8081)

### DOWN

```sh
podman compose down -v
```

### Kafka UI

Access Kafka UI at: http://localhost:8080

### structure

```txt
- source-events --|
                 [j]----
- players       --|    |
                      [*]--[G]--[A]--> store <-- query
                       |
- products      -------

* : join
G : group
A : aggregate 
```

### ref
- https://www.conduktor.io/glossary/state-stores-in-kafka-streams
- 