package org.mohadang.demoapp.serde

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

class TweetSerdes : Serde<Tweet> {
    override fun serializer(): Serializer<Tweet> {
        return TweetSerializer()
    }

    override fun deserializer(): Deserializer<Tweet> {
        return TweetDeserializer()
    }
}