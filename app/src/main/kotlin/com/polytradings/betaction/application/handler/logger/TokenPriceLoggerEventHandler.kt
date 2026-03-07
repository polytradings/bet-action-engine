package com.polytradings.betaction.application.handler.logger

import com.polytradings.betaction.protobuf.TokenPriceProto
import org.slf4j.LoggerFactory

class TokenPriceLoggerEventHandler(active: Boolean = true) : LoggerEventHandler(active) {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun processEvent(eventData: ByteArray) {
        val tokenPrice = TokenPriceProto.TokenPrice.parseFrom(eventData)
        logger.info(
            "TokenPrice event - source: ${tokenPrice.source}, " +
                    "marketId: ${tokenPrice.marketId}, " +
                    "conditionId: ${tokenPrice.conditionId}, " +
                    "tokenId: ${tokenPrice.tokenId}, " +
                    "side: ${tokenPrice.side}, " +
                    "price: ${tokenPrice.price}, " +
                    "timestampUnixMs: ${tokenPrice.timestampUnixMs}"
        )
    }
}
