echo "Waiting for Kafka to come online..."

cub kafka-ready -b kafka:29092 1 20

# produce score events keyed by playerId (key|value format)
sed -E 's/.*playerId.:([0-9]+).*/\1|&/' score-event-test.json |
  kafka-console-producer \
    --bootstrap-server kafka:29092 \
    --topic score-events \
    --property parse.key=true \
    --property key.separator='|'
