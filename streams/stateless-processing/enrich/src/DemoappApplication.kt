package org.mohadang.demoapp

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.Properties

@SpringBootApplication
class DemoappApplication

fun main(args: Array<String>) {
    runApplication<DemoappApplication>(*args)

    val topology = CryptoTopology.build()
    val config = Properties()
    config[StreamsConfig.APPLICATION_ID_CONFIG] = "dev3"
    config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"

    val streams = KafkaStreams(topology, config)

    // shutdown hook - process kill signal
    Runtime.getRuntime().addShutdownHook(Thread(Runnable { streams.close() }))

    streams.start()
}
