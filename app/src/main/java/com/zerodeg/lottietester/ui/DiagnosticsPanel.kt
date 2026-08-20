package com.zerodeg.lottietester.ui

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zerodeg.lottietester.model.LoadedSource
import java.util.Locale

@Composable
fun DiagnosticsPanel(source: LoadedSource, rendererName: String, report: RendererReport) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    val text = buildDiagnosticsText(source, rendererName, report)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("진단", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("Lottie 진단", text)) }) {
                    Text("결과 복사")
                }
            }
            text.lines().forEach { line ->
                Text(line, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun buildDiagnosticsText(source: LoadedSource, rendererName: String, report: RendererReport): String = buildString {
    appendLine("렌더러: $rendererName")
    appendLine("소스: ${source.source.label}")
    appendLine("감지 형식: ${source.inspection.format.label}")
    appendLine("MIME: ${source.inspection.mimeType ?: "알 수 없음"}")
    appendLine("파일 크기: ${formatBytes(source.inspection.sizeBytes)}")
    appendLine("입력 로딩: ${source.loadMillis} ms")
    appendLine("animation JSON 수: ${source.inspection.animationJsonCount}")
    source.inspection.warnings.forEach { appendLine("입력 경고: $it") }
    report.diagnostics?.let { info ->
        appendLine("캔버스: ${info.width} × ${info.height}")
        appendLine("FPS: ${String.format(Locale.US, "%.2f", info.frameRate)}")
        appendLine("프레임: ${String.format(Locale.US, "%.1f", info.startFrame)} ~ ${String.format(Locale.US, "%.1f", info.endFrame)}")
        appendLine("재생 시간: ${info.durationMillis} ms")
        appendLine("이미지/폰트: ${info.imageCount} / ${info.fontCount}")
        if (info.warnings.isEmpty()) appendLine("Lottie 경고: 없음")
        info.warnings.forEach { appendLine("Lottie 경고: $it") }
    } ?: appendLine("Composition: 로딩 중 또는 실패")
    report.error?.let { appendLine("오류: $it") }
}.trimEnd()

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
    else -> "$bytes B"
}
