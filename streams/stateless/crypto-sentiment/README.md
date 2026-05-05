
### run containers

```bash
docker compose up -d
```

```bash
podman compose up -d
```

- Kafka broker (port 9092)
- Kafka UI (port 8080)
- Schema Registry (port 8081)

### Kafka UI

Access Kafka UI at: http://localhost:8080

### delete containers

```bash
docker compose down
```

```bash
podman compose down
```

To also remove volumes:

```bash
docker compose down -v
```
