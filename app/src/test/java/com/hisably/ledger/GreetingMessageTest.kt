package com.hisably.ledger

import org.junit.Assert.assertTrue
import org.junit.Test

class GreetingMessageTest {
    @Test
    fun subtitleConfirmsApkInstalled() {
        val subtitle = GreetingMessage.subtitle()

        assertTrue(subtitle.contains("APK installed successfully"))
        assertTrue(subtitle.contains("running correctly"))
    }
}
