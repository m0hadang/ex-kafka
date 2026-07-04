echo "Waiting for Kafka to come online..."

cub kafka-ready -b kafka:29092 1 20

# create the tweets topic
kafka-topics \
  --bootstrap-server kafka:29092 \
  --topic score-events \
  --replication-factor 1 \
  --partitions 3 \
  --create

sleep infinity
