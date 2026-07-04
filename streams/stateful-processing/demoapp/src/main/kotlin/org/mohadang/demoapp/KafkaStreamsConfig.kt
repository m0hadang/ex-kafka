package org.mohadang.demoapp

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Printed
import org.mohadang.demoapp.model.ScoreEvent
import org.mohadang.demoapp.serde.JsonSerde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams

@Configuration
@EnableKafkaStreams
class KafkaStreamsConfig {

    @Bean
    fun leaderboardTopology(
        streamsBuilder: StreamsBuilder
    ): KStream<String, ScoreEvent> {
        val kstream = streamsBuilder
            .stream("score-events", Consumed.with(Serdes.String(), JsonSerde(ScoreEvent::class.java)))

        kstream.print(
            Printed.toSysOut()
        )

        return kstream
    }
}