package com.polytradings.betaction.domain.port

import com.polytradings.betaction.domain.model.TopicSubscribe

interface EventListenerPort {
    fun subscribe(vararg topicSubscribers: TopicSubscribe): EventListenerPort
    fun start(): EventListenerPort
    fun stop()
}
