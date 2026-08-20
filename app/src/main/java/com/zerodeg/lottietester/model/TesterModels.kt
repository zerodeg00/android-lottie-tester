package com.zerodeg.lottietester.model

import android.net.Uri

enum class LottieFileFormat(val label: String) {
    JSON("JSON"),
    DOT_LOTTIE(".lottie / ZIP"),
}

sealed interface SourceRef {
    val label: String

    data class Remote(val url: String) : SourceRef {
        override val label: String = url
    }

    data class Local(val uri: Uri, override val label: String) : SourceRef
}

data class SourceInspection(
    val format: LottieFileFormat,
    val sizeBytes: Long,
    val mimeType: String?,
    val animationJsonCount: Int,
    val warnings: List<String>,
)

data class LoadedSource(
    val id: Long,
    val source: SourceRef,
    val bytes: ByteArray,
    val inspection: SourceInspection,
    val loadMillis: Long,
)

sealed interface LoadState {
    data object Idle : LoadState
    data object Loading : LoadState
    data class Ready(val source: LoadedSource) : LoadState
    data class Failed(val message: String) : LoadState
}

data class TesterUiState(
    val urlInput: String = "",
    val loadState: LoadState = LoadState.Idle,
)

enum class TesterRenderMode(val label: String) {
    AUTOMATIC("자동"),
    HARDWARE("하드웨어"),
    SOFTWARE("소프트웨어"),
}

enum class PreviewScale(val label: String) {
    FIT("Fit"),
    FILL("Fill"),
    CROP("Crop"),
}

data class PlaybackSettings(
    val isPlaying: Boolean,
    val speed: Float,
    val repeat: Boolean,
    val renderMode: TesterRenderMode,
    val scale: PreviewScale,
    val backgroundIndex: Int = 0,
    val restartToken: Int = 0,
    val seekProgress: Float? = null,
    val seekToken: Int = 0,
) {
    companion object {
        fun viewDefaults() = PlaybackSettings(
            isPlaying = true,
            speed = 1f,
            repeat = false,
            renderMode = TesterRenderMode.AUTOMATIC,
            scale = PreviewScale.FIT,
        )

        fun composeDefaults() = PlaybackSettings(
            isPlaying = true,
            speed = 1f,
            repeat = true,
            renderMode = TesterRenderMode.HARDWARE,
            scale = PreviewScale.FILL,
        )
    }
}

data class CompositionDiagnostics(
    val width: Int,
    val height: Int,
    val frameRate: Float,
    val startFrame: Float,
    val endFrame: Float,
    val durationMillis: Long,
    val imageCount: Int,
    val fontCount: Int,
    val warnings: List<String>,
)
