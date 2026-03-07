package com.polytradings.betaction.application.handler.logger

import com.polytradings.betaction.domain.model.EventHandler
import org.slf4j.LoggerFactory

abstract class LoggerEventHandler(private val active: Boolean) : EventHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handle(eventData: ByteArray) {
        if (!active) {
            logger.debug("${javaClass.simpleName} is inactive, skipping event")
            return
        }
        try {
            processEvent(eventData)
        } catch (e: Exception) {
            logger.error("Error processing event in ${javaClass.simpleName}", e)
        }
    }

    protected abstract fun processEvent(eventData: ByteArray)
}