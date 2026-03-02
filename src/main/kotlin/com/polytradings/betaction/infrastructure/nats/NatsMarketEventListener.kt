package com.polytradings.betaction.infrastructure.nats

import com.polytradings.betaction.domain.port.MarketEventPort
import io.nats.client.Connection
import io.nats.client.Dispatcher
import org.slf4j.LoggerFactory

class NatsMarketEventListener(
    private val natsConnection: Connection,
    private val topic: String
) : MarketEventPort {

    private val logger = LoggerFactory.getLogger(javaClass)
    @Volatile
    private var dispatcher: Dispatcher? = null
    private var eventHandler: ((ByteArray) -> Unit)? = null

    override fun subscribe(onEvent: (ByteArray) -> Unit) {
        this.eventHandler = onEvent
    }

    override fun start() {
        synchronized(this) {
            if (dispatcher != null) {
                logger.warn("Listener already started")
                return
            }

            dispatcher = natsConnection.createDispatcher { msg ->
                try {
                    eventHandler?.invoke(msg.data)
                } catch (e: Exception) {
                    logger.error("Error processing market event", e)
                }
            }

            dispatcher?.subscribe(topic)
        }
        logger.info("NatsMarketEventListener started on topic: $topic")
    }

    override fun stop() {
        synchronized(this) {
            dispatcher?.unsubscribe(topic)
            dispatcher = null
        }
        logger.info("NatsMarketEventListener stopped")
    }
}
