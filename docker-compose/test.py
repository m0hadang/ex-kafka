from kafka import KafkaProducer
from json import dumps
import time

producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    key_serializer=lambda x: x.encode('utf-8'),
    value_serializer=lambda x: x.encode('utf-8'),
    # value_serializer=lambda x: dumps(x).encode('utf-8'),  # json serialize
)

start = time.time()

for i in range(30):

    data = 'result' + str(i)
    # data = {'str': 'result' + str(i)}

    partition_key = 'even' if i % 2 == 0 else 'odd'
    if i % 10 == 0:
        partition_key = '10x'

    producer.send('my-topic', key=partition_key, value=data)
    producer.flush()

print('[Done]:', time.time() - start)