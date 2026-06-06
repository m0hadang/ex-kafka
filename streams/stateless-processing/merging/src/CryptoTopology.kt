package org.mohadang.demoapp

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Branched
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.kstream.Predicate
import org.apache.kafka.streams.kstream.Printed
import org.mohadang.demoapp.serde.Tweet
import org.mohadang.demoapp.serde.TweetSerdes

class CryptoTopology {
    companion object {
        fun basicBuild(): Topology {
            val builder = StreamsBuilder()

            val kstream = builder
                .stream("tweets", Consumed.with(Serdes.String(), TweetSerdes()))
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

            return builder.build()
        }

        private fun translate(tweet: Tweet): Tweet {
            val clone = tweet.copy()
            clone.text = "<<Translated>>: " + tweet.text
            return clone
        }
    }
}