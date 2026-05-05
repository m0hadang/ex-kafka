echo "Waiting for Kafka to come online..."

cub kafka-ready -b kafka:29092 1 20

# create the tweets topic
kafka-topics \
  --bootstrap-server kafka:29092 \
  --topic tweets \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create the crypto-sentiment topic
kafka-topics \
  --bootstrap-server kafka:29092 \
  --topic crypto-sentiment \
  --replication-factor 1 \
  --partitions 4 \
  --create

sleep infinity
