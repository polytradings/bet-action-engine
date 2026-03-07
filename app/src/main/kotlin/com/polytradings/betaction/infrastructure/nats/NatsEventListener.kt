package com.polytradings.betaction.infrastructure.nats

import com.polytradings.betaction.config.NatsConfig
import com.polytradings.betaction.domain.model.TopicSubscribe
import com.polytradings.betaction.domain.port.EventListenerPort
import io.nats.client.Connection
import io.nats.client.Dispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory

@OptIn(ExperimentalCoroutinesApi::class)
class NatsEventListener : EventListenerPort {
    private val natsConnection: Connection = NatsConfig.connection
    private val logger = LoggerFactory.getLogger(javaClass)

    private val dispatchers = mutableMapOf<String, Dispatcher>()
    private val subscribedTopics = mutableSetOf<String>()

    private val handlerSupervisorJob = SupervisorJob()
    private val handlerParallelism = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
    private val handlerScope = CoroutineScope(
        Dispatchers.IO.limitedParallelism(handlerParallelism) +
            handlerSupervisorJob +
            CoroutineName("nats-event-handler")
    )

    @Volatile
    private var acceptingMessages = true

    override fun subscribe(vararg topicSubscribers: TopicSubscribe): EventListenerPort {
        synchronized(this) {
            // Subscribe to each topic with its own dispatcher
            topicSubscribers.forEach { (topic, handlers) ->
                if (subscribedTopics.contains(topic)) {
                    logger.info("Already subscribed to topic: $topic")
                } else {
                    // Create a dedicated dispatcher for this topic
                    val dispatcher = natsConnection.createDispatcher { msg ->
                        if (!acceptingMessages) {
                            logger.debug("Ignoring message while stopping listener: ${msg.subject}")
                            return@createDispatcher
                        }

                        logger.debug("Received message on topic: ${msg.subject}, data size: ${msg.data.size} bytes")

                        handlerScope.launch {
                            handlers.map { handler ->
                                launch {
                                    try {
                                        handler.handle(msg.data)
                                    } catch (e: Exception) {
                                        logger.error("Error in handler on topic: ${msg.subject}", e)
                                    }
                                }
                            }.joinAll()
                        }
                    }

                    // Store the dispatcher
                    dispatchers[topic] = dispatcher
                    subscribedTopics.add(topic)

                    logger.info("Subscribed to topic: $topic with dedicated dispatcher")
                }
            }
        }
        return this
    }

    override fun start(): EventListenerPort {
        synchronized(this) {
            if (dispatchers.isEmpty()) {
                logger.warn("Listener not subscribed yet. Call subscribe() first")
            } else {
                for ((topic, dispatcher) in dispatchers.entries) {
                    dispatcher.subscribe(topic)
                    logger.info("Dispatcher for topic '$topic' started")
                }
            }
            return this
        }
    }


    override fun stop() {
        synchronized(this) {
            logger.info("Stopping NatsEventListener...")
            acceptingMessages = false

            if (dispatchers.isNotEmpty()) {
                dispatchers.forEach { (topic, dispatcher) ->
                    dispatcher.unsubscribe(topic)
                    logger.info("Unsubscribed from topic: $topic")
                }
                dispatchers.clear()
                subscribedTopics.clear()
                logger.info("All dispatchers stopped")
            } else {
                logger.info("No dispatchers to stop")
            }
        }

        runBlocking {
            val gracefulStop = withTimeoutOrNull(10_000) {
                handlerSupervisorJob.children.toList().joinAll()
                true
            } == true

            if (!gracefulStop) {
                logger.warn("Timeout waiting in-flight handlers; cancelling remaining coroutines")
            }

            handlerScope.cancel()
        }
    }
}
