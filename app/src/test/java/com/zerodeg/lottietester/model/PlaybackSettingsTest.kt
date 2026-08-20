package com.zerodeg.lottietester.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSettingsTest {
    @Test
    fun `View preset uses single-play automatic rendering defaults`() {
        val preset = PlaybackSettings.viewDefaults()

        assertTrue(preset.isPlaying)
        assertFalse(preset.repeat)
        assertEquals(1f, preset.speed)
        assertEquals(TesterRenderMode.AUTOMATIC, preset.renderMode)
    }

    @Test
    fun `Compose preset uses looping hardware rendering defaults`() {
        val preset = PlaybackSettings.composeDefaults()

        assertTrue(preset.isPlaying)
        assertTrue(preset.repeat)
        assertEquals(1f, preset.speed)
        assertEquals(TesterRenderMode.HARDWARE, preset.renderMode)
        assertEquals(PreviewScale.FILL, preset.scale)
    }
}
