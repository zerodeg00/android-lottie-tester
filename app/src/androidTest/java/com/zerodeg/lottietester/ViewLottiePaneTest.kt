package com.zerodeg.lottietester

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.zerodeg.lottietester.model.LoadedSource
import com.zerodeg.lottietester.model.LottieFileFormat
import com.zerodeg.lottietester.model.PlaybackSettings
import com.zerodeg.lottietester.model.SourceInspection
import com.zerodeg.lottietester.model.SourceRef
import com.zerodeg.lottietester.ui.RendererReport
import com.zerodeg.lottietester.ui.ViewLottiePane
import org.junit.Rule
import org.junit.Test

class ViewLottiePaneTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun viewRendererStopsLoadingAfterCompositionIsParsed() {
        val json = """{"v":"5.7.4","fr":30,"ip":0,"op":30,"w":100,"h":100,"nm":"test","ddd":0,"assets":[],"layers":[]}"""
        val source = LoadedSource(
            id = 99L,
            source = SourceRef.Local(Uri.EMPTY, "test.json"),
            bytes = json.toByteArray(),
            inspection = SourceInspection(
                format = LottieFileFormat.JSON,
                sizeBytes = json.length.toLong(),
                mimeType = "application/json",
                animationJsonCount = 1,
                warnings = emptyList(),
            ),
            loadMillis = 1L,
        )
        val report = mutableStateOf(RendererReport())

        composeRule.setContent {
            ViewLottiePane(
                source = source,
                settings = PlaybackSettings.viewDefaults(),
                onProgress = {},
                onReport = { report.value = it },
            )
        }

        composeRule.waitUntil(timeoutMillis = 5_000) { report.value.diagnostics != null }
        composeRule.onNodeWithTag("view-loading").assertDoesNotExist()
    }
}
