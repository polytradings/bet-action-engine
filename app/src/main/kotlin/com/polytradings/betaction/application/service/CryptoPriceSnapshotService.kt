package com.polytradings.betaction.application.service

import com.polytradings.betaction.config.EnvAppReader
import com.polytradings.betaction.domain.model.CryptoPrice
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class CryptoPriceSnapshotService {
    private val sourcePriority: List<String> = EnvAppReader.cryptoPriceSourcePriority
    private val logger = LoggerFactory.getLogger(javaClass)

    // Thread-safe map to store latest prices by symbol
    private val priceSnapshot = ConcurrentHashMap<String, CryptoPrice>()

    /**
     * Updates the snapshot with a new crypto price.
     * Only updates if:
     * 1. No previous price exists for the symbol, OR
     * 2. New price is from a higher priority source, OR
     * 3. Same source but newer timestamp
     */
    fun updatePrice(symbol: String, price: Double, source: String, timestamp: Instant) {
        val normalizedSymbol = symbol.uppercase()
        val newPrice = CryptoPrice(normalizedSymbol, price, source, timestamp)
        priceSnapshot.compute(normalizedSymbol) { _, currentPrice ->
            when {
                currentPrice == null -> {
                    logger.debug("First price for $normalizedSymbol: $price from $source")
                    newPrice
                }

                shouldUpdate(currentPrice, newPrice) -> {
                    logger.debug(
                        "Updating $normalizedSymbol: $price from $source " +
                                "(previous: ${currentPrice.price} from ${currentPrice.source})"
                    )
                    newPrice
                }

                else -> {
                    logger.trace(
                        "Ignoring price for $normalizedSymbol from $source " +
                                "(current source ${currentPrice.source} has higher priority)"
                    )
                    currentPrice
                }
            }
        }
    }

    /**
     * Determines if the new price should replace the current one.
     * Priority: source priority > timestamp
     */
    private fun shouldUpdate(current: CryptoPrice, new: CryptoPrice): Boolean {
        val currentSourcePriority = getSourcePriority(current.source)
        val newSourcePriority = getSourcePriority(new.source)
        return when {
            newSourcePriority < currentSourcePriority -> true
            newSourcePriority > currentSourcePriority -> false
            else -> new.timestamp.isAfter(current.timestamp)
        }
    }

    /**
     * Returns the priority index of a source (lower = higher priority).
     */
    private fun getSourcePriority(source: String): Int {
        val index = sourcePriority.indexOfFirst { it.equals(source, ignoreCase = true) }
        return if (index >= 0) index else sourcePriority.size
    }

    /**
     * Gets the latest price for a symbol.
     */
    fun getLatestPrice(symbol: String): CryptoPrice? {
        return priceSnapshot[symbol.uppercase()]
    }

    /**
     * Gets all latest prices.
     */
    fun getAllPrices(): Map<String, CryptoPrice> {
        return priceSnapshot.toMap()
    }

    /**
     * Clears all stored prices.
     */
    fun clear() {
        val count = priceSnapshot.size
        priceSnapshot.clear()
        logger.info("Cleared $count crypto prices from snapshot")
    }

    /**
     * Gets the count of stored symbols.
     */
    fun getSymbolCount(): Int = priceSnapshot.size
}
