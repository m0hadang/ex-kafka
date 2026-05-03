- create topic
    - > docker exec -it kafka-1 kafka-topics --create --topic test-topic --bootstrap-server kafka-1:29092 --partitions 1 --replication-factor 1

- delete topic
    - > docker exec kafka-1 kafka-topics --bootstrap-server kafka-1:29092 --delete --topic test-topic
    * 다른 브로커 통해 생성한 토픽도 제거 가능

- consume
    - > docker exec -it kafka-1 kafka-console-consumer --bootstrap-server kafka-1:29092,kafka-2:29093 --topic test-topic --from-beginning

- produce
    - > docker exec -it kafka-1 kafka-console-producer --bootstrap-server kafka-1:29092,kafka-2:29093 --topic test-topic

- connet zookeeper shell
    - > docker exec -it zookeeper-1 /usr/bin/zookeeper-shell localhos

- --bootstrap-server
    - Kafka commands specifies the address of one or more Kafka brokers that the command should connect to.
    - ex)
        - --bootstrap-server kafka-1:29092
            - docker container host:port 지정 가능
        - --bootstrap-server localhost:9092
            - docker container port 바인딩 된 주소도 사용 가능
        - --bootstrap-server localhost:9092,localhost:9093
            - 여러 host 도 지정 가능