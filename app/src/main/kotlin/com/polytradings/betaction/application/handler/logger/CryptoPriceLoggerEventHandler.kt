package com.polytradings.betaction.application.handler.logger

import com.polytradings.betaction.protobuf.CryptoPriceProto
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class CryptoPriceLoggerEventHandler(active: Boolean = true) : LoggerEventHandler(active) {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun processEvent(eventData: ByteArray) {
        val cryptoPrice = CryptoPriceProto.CryptoPrice.parseFrom(eventData)
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(cryptoPrice.timestampUnixMs),
            ZoneId.systemDefault()
        )

        logger.info(
            "CryptoPrice event - source: ${cryptoPrice.source}, " +
                    "symbol: ${cryptoPrice.symbol}, " +
                    "price: ${cryptoPrice.price}, " +
                    "timestamp: $dateTime (${cryptoPrice.timestampUnixMs} ms)"
        )
    }
}


