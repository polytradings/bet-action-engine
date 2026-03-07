package com.polytradings.betaction.application.reporter

import com.polytradings.betaction.application.service.CryptoPriceSnapshotService
import org.slf4j.Logger

class AppLifecycleReporter(
    private val logger: Logger
) {
    private val separator = "========================================"

    private fun logWithSeparator(intermediateLog: () -> Unit) {
        logger.info(separator)
        intermediateLog()
        logger.info(separator)
    }

    fun reportStart() {
        logWithSeparator {
            logger.info("Starting BetActionApplication...")
        }
    }

    fun reportStartedSuccessfully() {
        logWithSeparator {
            logger.info("BetActionApplication started successfully")
        }
    }

    fun reportShutdown(
        reason: String,
        cryptoPriceSnapshotService: CryptoPriceSnapshotService,
        intermediateActions: () -> Unit
    ) {
        logWithSeparator {
            logger.info("Shutting down BetActionApplication... ($reason)")
        }
        reportShutdownSnapshot(cryptoPriceSnapshotService)
        intermediateActions()
        reportShutdownComplete()
    }

    private fun reportShutdownSnapshot(cryptoPriceSnapshotService: CryptoPriceSnapshotService) {
        logWithSeparator {
            logger.info("Final snapshot: ${cryptoPriceSnapshotService.getSymbolCount()} symbols stored")
            for ((symbol, cryptoPrice) in cryptoPriceSnapshotService.getAllPrices()) {
                logger.info(" - $symbol at ${cryptoPrice.timestamp}: ${cryptoPrice.price}")
            }
        }
    }

    private fun reportShutdownComplete() {
        logWithSeparator {
            logger.info("BetActionApplication shutdown complete")
        }
    }
}

