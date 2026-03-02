package com.polytradings.betaction.domain.port

interface MarketEventPort {
    fun subscribe(onEvent: (ByteArray) -> Unit)
    fun start()
    fun stop()
}
