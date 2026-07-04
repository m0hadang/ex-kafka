
### run containers

```sh
podman compose up -d
```

- Kafka broker (port 9092)
- Kafka UI (port 8080)
- Schema Registry (port 8081)

### delete containers

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