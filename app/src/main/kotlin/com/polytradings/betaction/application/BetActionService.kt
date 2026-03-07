package com.polytradings.betaction.application

import com.polytradings.betaction.domain.model.ActionStatus
import com.polytradings.betaction.domain.port.ActionPublisherPort
import com.polytradings.betaction.domain.port.EventListenerPort
import com.polytradings.betaction.protobuf.ActionEventProto
import com.polytradings.betaction.protobuf.CryptoPriceProto
import org.slf4j.LoggerFactory

class BetActionService(
    private val eventListenerPort: EventListenerPort,
    private val actionPublisherPort: ActionPublisherPort
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private var lastPublishedStatus: MutableMap<String, ActionStatus> = java.util.concurrent.ConcurrentHashMap()

    @Volatile
    private var eventCount: Long = 0
    /*
        fun start() {
            logger.info("BetActionService.start() called")
            try {
                marketEventPort.subscribe { eventData ->
                    eventCount++
                    logger.debug("Event #$eventCount received with size: ${eventData.size} bytes")
                    handleMarketEvent(eventData)
                }
                marketEventPort.start()
                logger.info("BetActionService started successfully - waiting for events on market event port")
            } catch (e: Exception) {
                logger.error("Error starting BetActionService", e)
                throw e
            }
        }

     */

    fun stop() {
        try {
            eventListenerPort.stop()
            actionPublisherPort.close()
            logger.info("BetActionService stopped - processed $eventCount events total")
        } catch (e: Exception) {
            logger.error("Error stopping BetActionService", e)
        }
    }

    private fun handleMarketEvent(eventData: ByteArray) {
        try {
            val cryptoPrice = CryptoPriceProto.CryptoPrice.parseFrom(eventData)
            if (cryptoPrice != null &&
                cryptoPrice.symbol.isNotEmpty() &&
                cryptoPrice.symbol.equals("btc", ignoreCase = true) //&&
            //cryptoPrice.marketId.startsWith("btc-updown-5m-1772667900", ignoreCase = true)
            ) {
                logger.info("Parsed CryptoPrice event: symbol=${cryptoPrice.symbol}, price=${cryptoPrice.price}")
            }
            val symbol = cryptoPrice.symbol
            val currentStatus = determineAction(cryptoPrice)

            val lastStatus = lastPublishedStatus[symbol]

            if (currentStatus != lastStatus) {
                publishActionEvent(symbol, currentStatus, cryptoPrice.timestampUnixMs)
                lastPublishedStatus[symbol] = currentStatus
                logger.info("Status changed for $symbol: $lastStatus -> $currentStatus")
            }
        } catch (e: Exception) {
            logger.error("Error handling market event", e)
        }
    }

    private fun determineAction(cryptoPrice: CryptoPriceProto.CryptoPrice): ActionStatus {
        // TODO: Implement action determination logic
        // This is a placeholder. Replace with actual business logic.
        return ActionStatus.NO_ACTION
    }

    private fun publishActionEvent(
        symbol: String,
        status: ActionStatus,
        timestamp: Long
    ) {
        val actionType = when (status) {
            ActionStatus.NO_ACTION -> ActionEventProto.ActionEvent.ActionType.NO_ACTION
            ActionStatus.WAIT -> ActionEventProto.ActionEvent.ActionType.WAIT
            ActionStatus.BET_DOWN -> ActionEventProto.ActionEvent.ActionType.BET_DOWN
            ActionStatus.BET_UP -> ActionEventProto.ActionEvent.ActionType.BET_UP
        }

        val actionEvent = ActionEventProto.ActionEvent.newBuilder()
            .setAction(actionType)
            .setSymbol(symbol)
            .setTimestamp(timestamp)
            .setReason("Action determined by BetActionService")
            .build()

        actionPublisherPort.publish(actionEvent.toByteArray())
    }
}
