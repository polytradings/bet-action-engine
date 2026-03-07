package com.polytradings.betaction.config

import java.util.*

object EnvAppReader {
    var natsServers: String = "nats://localhost:4222"
        private set

    var cryptoPriceSourcePriority: List<String> = listOf("polymarket", "binance")
        private set

    private val config: Properties by lazy {
        val props = Properties()
        val stream = requireNotNull(object {}.javaClass.getResourceAsStream("/application.properties")) {
            "File /application.properties not found in classpath"
        }
        stream.use { props.load(it) }
        props
    }

    init {
        loadFromSystem()
    }

    private fun loadFromSystem() {
        natsServers = System.getenv("NATS_SERVERS")
            ?.takeIf { it.isNotBlank() }
            ?: config.getProperty("nats.servers", natsServers)

        cryptoPriceSourcePriority = System.getenv("CRYPTO_PRICE_SOURCE_PRIORITY")
            ?.takeIf { it.isNotBlank() }?.split(",")?.map { it.trim() }
            ?: config.getProperty("cryptoPriceSourcePriority", cryptoPriceSourcePriority.toString())
                .split(",")
                .map { it.trim() }
    }
}