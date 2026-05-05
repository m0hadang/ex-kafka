package org.mohadang.cryptosentiment

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class CryptoSentimentApplication

fun main(args: Array<String>) {
    runApplication<CryptoSentimentApplication>(*args)

    val topology = CryptoTopology.basicBuild()
    val config = Properties()
    config[StreamsConfig.APPLICATION_ID_CONFIG] = "dev"
    config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    config[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    config[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

    val streams = KafkaStreams(topology, config)

    // shutdown hook - process kill signal
    Runtime.getRuntime().addShutdownHook(Thread(Runnable { streams.close() }))

    streams.start()
}
