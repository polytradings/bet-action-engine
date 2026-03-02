package com.polytradings.betaction.config

import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory

class NatsConfig(
    private val servers: String = "nats://localhost:4222"
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun createConnection(): io.nats.client.Connection {
        return try {
            val options = Options.Builder()
                .servers(servers.split(",").toTypedArray())
                .build()

            val connection = Nats.connect(options)
            logger.info("Connected to NATS server: $servers")
            connection
        } catch (e: Exception) {
            logger.error("Failed to connect to NATS server", e)
            throw e
        }
    }
}
