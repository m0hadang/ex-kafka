package org.mohadang.demoapp.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer

class TweetDeserializer : Deserializer<Tweet> {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun deserialize(topic: String?, data: ByteArray?): Tweet? {
        if (data == null) {
            return null
        }
        return try {
            objectMapper.readValue(data, Tweet::class.java)
        } catch (e: Exception) {
            throw SerializationException("Failed to deserialize Tweet from topic '$topic'", e)
        }
    }
}
