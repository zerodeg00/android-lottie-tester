package com.zerodeg.lottietester.ui

import com.airbnb.lottie.LottieComposition
import com.zerodeg.lottietester.model.CompositionDiagnostics

fun LottieComposition.toDiagnostics(): CompositionDiagnostics = CompositionDiagnostics(
    width = bounds.width(),
    height = bounds.height(),
    frameRate = frameRate,
    startFrame = startFrame,
    endFrame = endFrame,
    durationMillis = duration.toLong(),
    imageCount = images.size,
    fontCount = fonts.size,
    warnings = warnings.toList(),
)
