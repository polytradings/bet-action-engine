package com.polytradings.betaction.domain.port

interface ActionPublisherPort {
    fun publish(actionEvent: ByteArray)
    fun close()
}
