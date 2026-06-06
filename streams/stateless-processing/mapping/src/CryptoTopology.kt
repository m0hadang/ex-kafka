package org.mohadang.demoapp

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Branched
import org.apache.kafka.streams.kstream.Consumed
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

            kstream
                .split()
                .branch(
                    Predicate { _, tweet -> tweet.lang == "en" },
                    Branched.withConsumer { branch ->
                        branch.print(Printed.toSysOut<String?, Tweet?>().withLabel("english-tweets"))
                    }
                )
                .defaultBranch(
                    Branched.withConsumer { branch ->
                        branch
                            .mapValues { _, tweet -> translate(tweet) }
                            .print(Printed.toSysOut<String?, Tweet?>().withLabel("other-tweets"))
                    }
                )

//            kstream.print(Printed.toSysOut<String?, Tweet?>().withLabel("tweets-stream"))

            return builder.build()
        }

        private fun translate(tweet: Tweet): Tweet {
            val clone = tweet.copy()
            clone.text = "<<Translated>>: " + tweet.text
            return clone
        }
    }
}