package org.mohadang.demoapp.model

data class ScoreEvent(
    val playerId: Long? = null,
    val productId: Long? = null,
    val score: Double? = null,
)
