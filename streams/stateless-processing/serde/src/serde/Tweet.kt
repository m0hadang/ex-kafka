package org.mohadang.demoapp.serde

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Tweet(
    @JsonProperty("CreatedAt") val createdAt: Long,
    @JsonProperty("Id") val id: Long,
    @JsonProperty("Lang") val lang: String,
    @JsonProperty("Retweet") val retweet: Boolean,
    @JsonProperty("Text") val text: String,
)