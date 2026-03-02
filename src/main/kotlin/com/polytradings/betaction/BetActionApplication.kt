package com.polytradings.betaction

import com.polytradings.betaction.application.BetActionService
import com.polytradings.betaction.config.NatsConfig
import com.polytradings.betaction.infrastructure.nats.NatsActionPublisher
import com.polytradings.betaction.infrastructure.nats.NatsMarketEventListener
import org.slf4j.LoggerFactory
import java.util.Properties

fun main() {
    val logger = LoggerFactory.getLogger("BetActionApplication")

    try {
        // Load configuration
        val config = Properties()
        config.load(object {}.javaClass.getResourceAsStream("/application.properties"))

        val natsServers = config.getProperty("nats.servers", "nats://localhost:4222")
        val inputTopic = config.getProperty("nats.topic.input", "MarketAggregatedPrice")
        val outputTopic = config.getProperty("nats.topic.output", "ActionEvent")

        // Initialize NATS connection
        val natsConfig = NatsConfig(natsServers)
        val natsConnection = natsConfig.createConnection()

        // Initialize domain ports
        val marketEventPort = NatsMarketEventListener(natsConnection, inputTopic)
        val actionPublisherPort = NatsActionPublisher(natsConnection, outputTopic)

        // Initialize service
        val betActionService = BetActionService(marketEventPort, actionPublisherPort)

        // Start service
        betActionService.start()

        // Keep application running
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Shutting down BetActionApplication...")
            betActionService.stop()
            natsConnection.close()
        })

        logger.info("BetActionApplication started successfully")
        Thread.currentThread().join()

    } catch (e: Exception) {
        logger.error("Failed to start BetActionApplication", e)
        throw e
    }
}
