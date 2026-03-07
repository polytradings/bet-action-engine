package com.polytradings.betaction.application.handler.logger

import com.polytradings.betaction.protobuf.MarketCreatedProto
import org.slf4j.LoggerFactory

class MarketCreatedLoggerEventHandler(active: Boolean = true) : LoggerEventHandler(active) {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun processEvent(eventData: ByteArray) {
        val marketCreated = MarketCreatedProto.MarketCreated.parseFrom(eventData)
        logger.info(
            "MarketCreated event - source: ${marketCreated.source}, " +
                    "marketId: ${marketCreated.marketId}, " +
                    "conditionId: ${marketCreated.conditionId}, " +
                    "cryptoSymbol: ${marketCreated.cryptoSymbol}, " +
                    "timeframeMinutes: ${marketCreated.timeframeMinutes}, " +
                    "startUnixMs: ${marketCreated.startUnixMs}, " +
                    "endUnixMs: ${marketCreated.endUnixMs}, " +
                    "closed: ${marketCreated.closed}"
        )
    }
}
