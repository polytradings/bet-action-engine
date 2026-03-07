package com.polytradings.betaction.domain.model

interface EventHandler {
    fun handle(eventData: ByteArray)
}