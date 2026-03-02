package com.polytradings.betaction.application

import com.polytradings.betaction.domain.model.ActionStatus
import com.polytradings.betaction.domain.port.ActionPublisherPort
import com.polytradings.betaction.domain.port.MarketEventPort
import com.polytradings.betaction.protobuf.ActionEventProto
import com.polytradings.betaction.protobuf.MarketAggregatedPriceProto
import org.slf4j.LoggerFactory

class BetActionService(
    private val marketEventPort: MarketEventPort,
    private val actionPublisherPort: ActionPublisherPort
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private var lastPublishedStatus: MutableMap<String, ActionStatus> = java.util.concurrent.ConcurrentHashMap()

    fun start() {
        marketEventPort.subscribe { eventData ->
            handleMarketEvent(eventData)
        }
        marketEventPort.start()
        logger.info("BetActionService started")
    }

    fun stop() {
        marketEventPort.stop()
        actionPublisherPort.close()
        logger.info("BetActionService stopped")
    }

    private fun handleMarketEvent(eventData: ByteArray) {
        try {
            val marketEvent = MarketAggregatedPriceProto.MarketAggregatedPrice.parseFrom(eventData)
            val symbol = marketEvent.symbol
            val currentStatus = determineAction(marketEvent)

            val lastStatus = lastPublishedStatus[symbol]

            if (currentStatus != lastStatus) {
                publishActionEvent(symbol, currentStatus, marketEvent.timestamp)
                lastPublishedStatus[symbol] = currentStatus
                logger.info("Status changed for $symbol: $lastStatus -> $currentStatus")
            }
        } catch (e: Exception) {
            logger.error("Error handling market event", e)
        }
    }

    private fun determineAction(marketEvent: MarketAggregatedPriceProto.MarketAggregatedPrice): ActionStatus {
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
