
### run containers

```sh
podman compose up -d
```

- Kafka broker (port 9092)
- Kafka UI (port 8080)
- Schema Registry (port 8081)

### test

consume data from the sink topic (`crypto-sentiment`)
```sh
podman compose exec schema-registry bash
```
```sh
kafka-avro-console-consumer --bootstrap-server kafka:29092 --topic crypto-sentiment --from-beginning
```

### delete containers

```sh
podman compose down
```

### Kafka UI

Access Kafka UI at: http://localhost:8080