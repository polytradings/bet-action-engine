package com.polytradings.betaction.infrastructure.nats

import com.polytradings.betaction.domain.port.ActionPublisherPort
import io.nats.client.Connection
import org.slf4j.LoggerFactory

class NatsActionPublisher(
    private val natsConnection: Connection,
    private val topic: String
) : ActionPublisherPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun publish(actionEvent: ByteArray) {
        try {
            natsConnection.publish(topic, actionEvent)
            logger.debug("Action event published to topic: $topic")
        } catch (e: Exception) {
            logger.error("Error publishing action event", e)
            throw e
        }
    }

    override fun close() {
        logger.info("NatsActionPublisher closed")
    }
}
