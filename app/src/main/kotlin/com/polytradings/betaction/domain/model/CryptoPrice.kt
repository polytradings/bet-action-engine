package com.polytradings.betaction.domain.model
import java.time.Instant
data class CryptoPrice(
    val symbol: String,
    val price: Double,
    val source: String,
    val timestamp: Instant
)
