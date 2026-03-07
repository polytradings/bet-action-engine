package com.polytradings.betaction

import com.polytradings.betaction.application.handler.logger.CryptoPriceLoggerEventHandler
import com.polytradings.betaction.application.handler.logger.MarketCreatedLoggerEventHandler
import com.polytradings.betaction.application.handler.logger.TokenPriceLoggerEventHandler
import com.polytradings.betaction.application.handler.snapshot.CryptoPriceSnapshotEventHandler
import com.polytradings.betaction.application.reporter.AppLifecycleReporter
import com.polytradings.betaction.application.service.CryptoPriceSnapshotService
import com.polytradings.betaction.config.NatsConfig
import com.polytradings.betaction.domain.model.TopicSubscribe
import com.polytradings.betaction.infrastructure.nats.NatsEventListener
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

fun main() {
    val logger = LoggerFactory.getLogger("BetActionApplication")
    val lifecycleReporter = AppLifecycleReporter(logger)

    lifecycleReporter.reportStart()

    try {
        // Initialize services
        val cryptoPriceSnapshotService = CryptoPriceSnapshotService()

        // Initialize event handlers
        // Loggers
        val marketCreatedHandler = MarketCreatedLoggerEventHandler(active = false)
        val cryptoPriceLoggerHandler = CryptoPriceLoggerEventHandler(active = false)
        val tokenPriceHandler = TokenPriceLoggerEventHandler(active = false)
        // Snapshots
        val cryptoPriceSnapshotHandler = CryptoPriceSnapshotEventHandler(cryptoPriceSnapshotService)

        // Initialize domain ports
        val listener = NatsEventListener()
            .subscribe(
                TopicSubscribe(topic = "market.created.v1", marketCreatedHandler),
                TopicSubscribe(topic = "crypto.prices.>", cryptoPriceLoggerHandler, cryptoPriceSnapshotHandler),
                TopicSubscribe(topic = "token.prices.>", tokenPriceHandler)
            )
            .start()

        val shuttingDown = AtomicBoolean(false)
        fun shutdown(reason: String) {
            if (!shuttingDown.compareAndSet(false, true)) return

            lifecycleReporter
                .reportShutdown(reason, cryptoPriceSnapshotService) {
                    listener.stop()
                    NatsConfig.close()
                }
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            shutdown("shutdown hook")
        })

        lifecycleReporter.reportStartedSuccessfully()

        try {
            Thread.currentThread().join()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    } catch (e: Exception) {
        logger.error("Failed to start BetActionApplication", e)
        throw e
    }
}
