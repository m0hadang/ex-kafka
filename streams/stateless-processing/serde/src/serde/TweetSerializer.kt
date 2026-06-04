package org.mohadang.demoapp.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer

class TweetSerializer : Serializer<Tweet> {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun serialize(topic: String?, data: Tweet?): ByteArray? {
        if (data == null) {
            return null
        }
        return try {
            objectMapper.writeValueAsBytes(data)
        } catch (e: Exception) {
            throw SerializationException("Failed to serialize Tweet for topic '$topic'", e)
        }
    }
}
