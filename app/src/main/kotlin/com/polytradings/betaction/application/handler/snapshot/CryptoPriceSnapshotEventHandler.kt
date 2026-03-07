package com.polytradings.betaction.application.handler.snapshot
import com.polytradings.betaction.application.service.CryptoPriceSnapshotService
import com.polytradings.betaction.domain.model.EventHandler
import com.polytradings.betaction.protobuf.CryptoPriceProto
import org.slf4j.LoggerFactory
import java.time.Instant
class CryptoPriceSnapshotEventHandler(
    private val snapshotService: CryptoPriceSnapshotService
) : EventHandler {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun handle(eventData: ByteArray) {
        try {
            val cryptoPrice = CryptoPriceProto.CryptoPrice.parseFrom(eventData)
            val timestamp = Instant.ofEpochMilli(cryptoPrice.timestampUnixMs)
            snapshotService.updatePrice(
                symbol = cryptoPrice.symbol,
                price = cryptoPrice.price,
                source = cryptoPrice.source,
                timestamp = timestamp
            )
            logger.trace(
                "Processed CryptoPrice snapshot - symbol: ${cryptoPrice.symbol}, " +
                "price: ${cryptoPrice.price}, source: ${cryptoPrice.source}"
            )
        } catch (e: Exception) {
            logger.error("Error processing CryptoPrice snapshot event", e)
        }
    }
}
