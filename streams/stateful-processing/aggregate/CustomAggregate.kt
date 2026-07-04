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
import org.mohadang.demoapp.model.ScoreStats
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

        val ktable = kstream
            .groupByKey(Grouped.with(Serdes.String(), JsonSerde(ScoreEvent::class.java)))
            .aggregate(
                { ScoreStats() },
                { _, event, stats ->
                    val score = event.score ?: 0.0
                    ScoreStats(
                        count = stats.count + 1,
                        sum = stats.sum + score,
                        max = maxOf(stats.max, score),
                    )
                },
                Materialized.`as`<String, ScoreStats, KeyValueStore<Bytes, ByteArray>>("score-events-stats")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(JsonSerde(ScoreStats::class.java))
            )

        ktable.toStream().print(
            Printed.toSysOut<String, ScoreStats>().withLabel("score-events-counts")
        )
        // ==> output
        // [score-events-counts]: 1, ScoreStats(count=3, sum=5300.0, max=3500.0)
        // [score-events-counts]: 2, ScoreStats(count=3, sum=10500.0, max=6000.0)
        // [score-events-counts]: 5, ScoreStats(count=2, sum=7800.0, max=7500.0)
        // [score-events-counts]: 3, ScoreStats(count=3, sum=14500.0, max=9000.0)
        // [score-events-counts]: 6, ScoreStats(count=2, sum=14000.0, max=9800.0)
        // [score-events-counts]: 4, ScoreStats(count=3, sum=6800.0, max=5100.0)

        return kstream
    }
}