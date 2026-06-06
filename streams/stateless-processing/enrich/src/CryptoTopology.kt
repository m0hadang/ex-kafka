package org.mohadang.demoapp

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Branched
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.kstream.Predicate
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.kstream.Produced
import org.mohadang.demoapp.serde.EntitySentiment
import org.mohadang.demoapp.serde.JsonSerde
import org.mohadang.demoapp.serde.Tweet
import kotlin.random.Random

class CryptoTopology {
    companion object {
        private val currencies = listOf("bitcoin", "ethereum")

        fun build(): Topology {
            val builder = StreamsBuilder()

            val kstream = builder
                .stream("tweets", Consumed.with(Serdes.String(), JsonSerde(Tweet::class.java)))
                .filterNot { _, tweet -> return@filterNot tweet.retweet }

            val branches = kstream
                .split(Named.`as`("branch-"))
                .branch(
                    Predicate { _, tweet -> tweet.lang == "en" },
                    Branched.`as`("english")
                )
                .defaultBranch(
                    Branched.`as`("other")
                )

            val englishStream = branches["branch-english"]!!
            val translatedStream = branches["branch-other"]!!
                .mapValues { _, tweet -> translate(tweet) }

            val merged = englishStream.merge(translatedStream)

            merged.print(Printed.toSysOut<String?, Tweet?>().withLabel("merged-tweets"))

            val enriched = merged
                .flatMapValues { _, tweet ->
                    getEntitySentiment(tweet)
                        .filter { currencies.contains(it.entity) }
                }

            enriched.print(Printed.toSysOut<String?, EntitySentiment?>().withLabel("crypto-sentiment"))

            // create new topic(crypto-sentiment)
            enriched.to(
                "crypto-sentiment",
                Produced.with(Serdes.String(), JsonSerde(EntitySentiment::class.java))
            )

            return builder.build()
        }
        private fun translate(tweet: Tweet): Tweet {
            val clone = tweet.copy()
            clone.text = "Translated: " + tweet.text
            return clone
        }

        private fun getEntitySentiment(tweet: Tweet): List<EntitySentiment> {
            val words = tweet.text.lowercase().replace("#", "").split(" ")
            return words.map { entity ->
                EntitySentiment(
                    createdAt = tweet.createdAt,
                    id = tweet.id,
                    entity = entity,
                    text = tweet.text,
                    salience = randomDouble(),
                    sentimentScore = randomDouble(),
                    sentimentMagnitude = randomDouble(),
                )
            }
        }

        private fun randomDouble(): Double = Random.nextDouble(0.0, 1.0)
    }
}
