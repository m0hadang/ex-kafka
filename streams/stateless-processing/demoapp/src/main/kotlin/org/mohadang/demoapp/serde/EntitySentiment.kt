package org.mohadang.demoapp.serde

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// mirrors the schema in avro/entity_sentiment.avsc, but serialized as JSON
// (no schema registry / Avro). Field names match the .avsc so the output
// matches the book's sink records.
@JsonIgnoreProperties(ignoreUnknown = true)
data class EntitySentiment(
    @JsonProperty("created_at") val createdAt: Long,
    @JsonProperty("id") val id: Long,
    @JsonProperty("entity") val entity: String,
    @JsonProperty("text") val text: String,
    @JsonProperty("sentiment_score") val sentimentScore: Double,
    @JsonProperty("sentiment_magnitude") val sentimentMagnitude: Double,
    @JsonProperty("salience") val salience: Double,
)
