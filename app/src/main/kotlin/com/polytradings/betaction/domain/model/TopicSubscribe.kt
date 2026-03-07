package com.polytradings.betaction.domain.model

data class TopicSubscribe(
    val topic: String,
    val handlers: List<EventHandler>
){
    constructor(topic: String, vararg handlers: EventHandler) : this(topic, handlers.toList())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TopicSubscribe) return false
        return topic == other.topic
    }

    override fun hashCode(): Int {
        return topic.hashCode()
    }
}