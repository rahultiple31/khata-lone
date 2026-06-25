package com.hisably.ledger

import org.junit.Assert.assertTrue
import org.junit.Test

class GreetingMessageTest {
    @Test
    fun subtitleMentionsKotlinAndroid() {
        val subtitle = GreetingMessage.subtitle()

        assertTrue(subtitle.contains("Kotlin Android"))
    }
}
