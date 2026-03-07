package com.polytradings.betaction.config

import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import java.time.Duration

object NatsConfig {

    private val logger = LoggerFactory.getLogger(javaClass)

    var servers: String = EnvAppReader.natsServers
        private set

    val connection: Connection by lazy {
        createConnection()
    }

    private fun createConnection(): Connection {
        return try {
            val options = Options.Builder()
                .servers(servers.split(",").toTypedArray())
                .pingInterval(Duration.ofSeconds(30))
                .maxReconnects(5)
                .reconnectWait(Duration.ofSeconds(1))
                .connectionName("bet-action-engine")
                .build()

            val connection = Nats.connect(options)
            logger.info("Connected to NATS server: $servers")
            connection
        } catch (e: Exception) {
            logger.error("Failed to connect to NATS server", e)
            throw e
        }
    }

    fun close() {
        try {
            if (connection.status == Connection.Status.CONNECTED) {
                connection.close()
                logger.info("NATS connection closed")
            }
        } catch (_: IllegalStateException) {
            // Connection was never initialized
            logger.debug("NATS connection was never initialized")
        } catch (e: Exception) {
            logger.error("Error closing NATS connection", e)
        }
    }
}
