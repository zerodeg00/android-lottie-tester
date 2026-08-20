package com.zerodeg.lottietester.ui

import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zerodeg.lottietester.model.CompositionDiagnostics
import com.zerodeg.lottietester.model.LoadedSource
import com.zerodeg.lottietester.model.LottieFileFormat
import com.zerodeg.lottietester.model.PlaybackSettings
import com.zerodeg.lottietester.model.PreviewScale
import com.zerodeg.lottietester.model.SourceRef
import com.zerodeg.lottietester.model.TesterRenderMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

data class RendererReport(
    val diagnostics: CompositionDiagnostics? = null,
    val error: String? = null,
)

@Composable
fun ViewLottiePane(
    source: LoadedSource,
    settings: PlaybackSettings,
    onProgress: (Float) -> Unit,
    onReport: (RendererReport) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val latestReport by rememberUpdatedState(onReport)
    var view by remember { mutableStateOf<LottieAnimationView?>(null) }
    var loading by remember(source.id) { mutableStateOf(true) }
    var error by remember(source.id) { mutableStateOf<String?>(null) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        PreviewBackground(settings.backgroundIndex) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { activityContext ->
                    LottieAnimationView(activityContext).also { animationView ->
                        animationView.addLottieOnCompositionLoadedListener { composition ->
                            loading = false
                            error = null
                            latestReport(RendererReport(diagnostics = composition.toDiagnostics()))
                        }
                        animationView.setFailureListener { throwable ->
                            loading = false
                            error = throwable.message ?: throwable::class.java.simpleName
                            latestReport(RendererReport(error = error))
                        }
                        view = animationView
                    }
                },
                update = { animationView ->
                    animationView.speed = settings.speed
                    animationView.repeatCount = if (settings.repeat) LottieDrawable.INFINITE else 0
                    animationView.setRenderMode(settings.renderMode.toLottieRenderMode())
                    animationView.scaleType = settings.scale.toImageScaleType()
                    if (settings.isPlaying && !animationView.isAnimating) animationView.resumeAnimation()
                    if (!settings.isPlaying && animationView.isAnimating) animationView.pauseAnimation()
                },
            )
        }
        if (loading) CircularProgressIndicator(modifier = Modifier.testTag("view-loading"))
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }

    LaunchedEffect(source.id, view) {
        val animationView = view ?: return@LaunchedEffect
        loading = true
        error = null
        onReport(RendererReport())
        animationView.cancelAnimation()
        try {
            val cacheKey = "view_${source.id}"
            val json = if (source.inspection.format == LottieFileFormat.JSON) {
                source.bytes.toString(Charsets.UTF_8)
            } else {
                null
            }
            val result = withContext(Dispatchers.IO) {
                if (json != null) {
                    LottieCompositionFactory.fromJsonStringSync(json, cacheKey)
                } else {
                    LottieCompositionFactory.fromInputStreamSync(context, source.bytes.inputStream(), cacheKey)
                }
            }
            val composition = result.value ?: throw result.exception ?: IllegalArgumentException("composition 파싱 실패")
            if (source.inspection.format == LottieFileFormat.JSON) {
                animationView.setAnimationFromJson(requireNotNull(json), cacheKey)
            } else {
                animationView.setComposition(composition)
            }
            loading = false
            error = null
            onReport(RendererReport(diagnostics = composition.toDiagnostics()))
            if (settings.isPlaying) animationView.playAnimation()
        } catch (throwable: Throwable) {
            loading = false
            error = throwable.message ?: throwable::class.java.simpleName
            onReport(RendererReport(error = error))
        }
    }

    LaunchedEffect(settings.restartToken, view) {
        if (settings.restartToken == 0) return@LaunchedEffect
        view?.progress = 0f
        if (settings.isPlaying) view?.playAnimation()
    }

    LaunchedEffect(settings.seekToken, view) {
        if (settings.seekToken == 0) return@LaunchedEffect
        settings.seekProgress?.let { view?.progress = it }
        if (settings.isPlaying) view?.resumeAnimation()
    }

    LaunchedEffect(view) {
        while (isActive) {
            view?.let { onProgress(it.progress) }
            delay(50)
        }
    }
}

@Composable
fun ComposeLottiePane(
    source: LoadedSource,
    settings: PlaybackSettings,
    onProgress: (Float) -> Unit,
    onReport: (RendererReport) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = remember(source.id) {
        when (val sourceRef = source.source) {
            is SourceRef.Local -> LottieCompositionSpec.ContentProvider(sourceRef.uri)
            is SourceRef.Remote -> LottieCompositionSpec.Url(sourceRef.url)
        }
    }
    val compositionResult = rememberLottieComposition(spec, cacheKey = "compose_${source.id}")
    val composition = compositionResult.value
    val animatable = rememberLottieAnimatable()
    val lifecycleOwner = LocalLifecycleOwner.current
    var lifecycleActive by remember { mutableStateOf(true) }
    var handledRestartToken by remember(source.id) { mutableIntStateOf(settings.restartToken) }
    var handledSeekToken by remember(source.id) { mutableIntStateOf(settings.seekToken) }
    var animatedSourceId by remember { mutableLongStateOf(-1L) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> lifecycleActive = false
                Lifecycle.Event.ON_RESUME -> lifecycleActive = true
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(composition, compositionResult.error) {
        when {
            composition != null -> onReport(RendererReport(diagnostics = composition.toDiagnostics()))
            compositionResult.error != null -> onReport(
                RendererReport(error = compositionResult.error?.message ?: compositionResult.error?.javaClass?.simpleName),
            )
            else -> onReport(RendererReport())
        }
    }

    LaunchedEffect(
        composition,
        settings.isPlaying,
        lifecycleActive,
        settings.speed,
        settings.repeat,
        settings.restartToken,
        settings.seekToken,
    ) {
        val loadedComposition = composition ?: return@LaunchedEffect
        val shouldRestart = settings.restartToken != handledRestartToken
        val shouldSeek = settings.seekToken != handledSeekToken
        val isNewSource = animatedSourceId != source.id
        val initialProgress = when {
            shouldSeek && settings.seekProgress != null -> settings.seekProgress
            shouldRestart || isNewSource -> 0f
            else -> animatable.progress
        } ?: 0f
        handledRestartToken = settings.restartToken
        handledSeekToken = settings.seekToken
        animatedSourceId = source.id
        animatable.snapTo(loadedComposition, initialProgress)
        if (settings.isPlaying && lifecycleActive) {
            animatable.animate(
                composition = loadedComposition,
                iterations = if (settings.repeat) LottieConstants.IterateForever else 1,
                speed = settings.speed,
                initialProgress = initialProgress,
            )
        }
    }

    LaunchedEffect(animatable.progress) { onProgress(animatable.progress) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        PreviewBackground(settings.backgroundIndex) {
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { animatable.progress },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = settings.scale.toContentScale(),
                    renderMode = settings.renderMode.toLottieRenderMode(),
                )
            }
        }
        if (compositionResult.isLoading) CircularProgressIndicator()
        compositionResult.error?.let {
            Text(it.message ?: it::class.java.simpleName, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun TesterRenderMode.toLottieRenderMode(): RenderMode = when (this) {
    TesterRenderMode.AUTOMATIC -> RenderMode.AUTOMATIC
    TesterRenderMode.HARDWARE -> RenderMode.HARDWARE
    TesterRenderMode.SOFTWARE -> RenderMode.SOFTWARE
}

private fun PreviewScale.toImageScaleType(): ImageView.ScaleType = when (this) {
    PreviewScale.FIT -> ImageView.ScaleType.FIT_CENTER
    PreviewScale.FILL -> ImageView.ScaleType.FIT_XY
    PreviewScale.CROP -> ImageView.ScaleType.CENTER_CROP
}

private fun PreviewScale.toContentScale(): ContentScale = when (this) {
    PreviewScale.FIT -> ContentScale.Fit
    PreviewScale.FILL -> ContentScale.FillBounds
    PreviewScale.CROP -> ContentScale.Crop
}
