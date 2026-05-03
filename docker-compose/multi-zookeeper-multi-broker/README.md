
zookeeper-2, zookeeper-3 의 Peer communication port 가 2888, 3888 과 달라도 zookeeper-1 과 통신 하는데 문제 없는 이유
- The ports in ZOOKEEPER_SERVERS are the **internal container ports** (2888, 3888)
- The different host port mappings (2889, 2890, etc.) are only for external access
- Inside the Docker network, containers communicate using their **internal ports**
- Each ZooKeeper node knows about all other nodes through this configuration