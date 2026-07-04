package org.mohadang.demoapp

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.state.KeyValueStore
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
    ): KStream<Long, ScoreEvent> {
        val kstream = streamsBuilder
            .stream("score-events", Consumed.with(Serdes.String(), JsonSerde(ScoreEvent::class.java)))
            .selectKey { _, event -> event.playerId ?: -1L }
        // ==> repartition topic created by selectKey
        // dev-6-score-events-counts-repartition

        val ktable = kstream
            .groupByKey(Grouped.with(Serdes.Long(), JsonSerde(ScoreEvent::class.java)))
            .count(
                Materialized.`as`<Long, Long, KeyValueStore<Bytes, ByteArray>>("score-events-counts")
            )
        // ==> changelog topic created
        // dev-6-score-events-counts-changelog

        kstream.print(
            Printed.toSysOut()
        )
        // ==> output
        // [KSTREAM-KEY-SELECT-0000000001]: 1, ScoreEvent(playerId=1, productId=1, score=1000.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 2, ScoreEvent(playerId=2, productId=1, score=2000.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 3, ScoreEvent(playerId=3, productId=1, score=4000.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 4, ScoreEvent(playerId=4, productId=1, score=500.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 1, ScoreEvent(playerId=1, productId=6, score=800.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 2, ScoreEvent(playerId=2, productId=6, score=2500.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 3, ScoreEvent(playerId=3, productId=6, score=9000.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 4, ScoreEvent(playerId=4, productId=6, score=1200.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 1, ScoreEvent(playerId=1, productId=1, score=3500.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 2, ScoreEvent(playerId=2, productId=6, score=6000.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 5, ScoreEvent(playerId=5, productId=1, score=7500.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 5, ScoreEvent(playerId=5, productId=6, score=300.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 3, ScoreEvent(playerId=3, productId=1, score=1500.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 6, ScoreEvent(playerId=6, productId=6, score=4200.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 6, ScoreEvent(playerId=6, productId=1, score=9800.0)
        // [KSTREAM-KEY-SELECT-0000000001]: 4, ScoreEvent(playerId=4, productId=6, score=5100.0)

        ktable.toStream().print(
            Printed.toSysOut<Long, Long>().withLabel("score-events-counts")
        )
        // ==> output
        // [score-events-counts]: 1, 6
        // [score-events-counts]: 2, 6
        // [score-events-counts]: 5, 2
        // [score-events-counts]: 3, 3
        // [score-events-counts]: 6, 2
        // [score-events-counts]: 4, 6

        return kstream
    }
}