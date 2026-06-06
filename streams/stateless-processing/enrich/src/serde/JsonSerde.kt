package org.mohadang.demoapp.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

// One JSON (Jackson) serde for any type - no schema registry needed.
// Usage: JsonSerde(Tweet::class.java)
class JsonSerde<T : Any>(private val type: Class<T>) : Serde<T> {

    private val mapper: ObjectMapper = jacksonObjectMapper()

    override fun serializer() = Serializer<T> { _, data ->
        data?.let { mapper.writeValueAsBytes(it) }
    }

    override fun deserializer() = Deserializer<T> { _, bytes ->
        bytes?.let { mapper.readValue(it, type) }
    }
}
