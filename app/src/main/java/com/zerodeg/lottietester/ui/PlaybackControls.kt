package com.zerodeg.lottietester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zerodeg.lottietester.model.PlaybackSettings
import com.zerodeg.lottietester.model.PreviewScale
import com.zerodeg.lottietester.model.TesterRenderMode
import java.util.Locale

@Composable
fun PlaybackControls(
    settings: PlaybackSettings,
    progress: Float,
    onSettingsChange: (PlaybackSettings) -> Unit,
    onResetPreset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSettingsChange(settings.copy(isPlaying = !settings.isPlaying)) }) {
                Text(if (settings.isPlaying) "일시정지" else "재생")
            }
            OutlinedButton(
                onClick = {
                    onSettingsChange(
                        settings.copy(
                            isPlaying = true,
                            restartToken = settings.restartToken + 1,
                            seekProgress = null,
                        ),
                    )
                },
            ) { Text("처음부터") }
            OutlinedButton(onClick = onResetPreset) { Text("기본값") }
        }

        Text("진행률 ${(progress.coerceIn(0f, 1f) * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = { value ->
                onSettingsChange(
                    settings.copy(
                        seekProgress = value,
                        seekToken = settings.seekToken + 1,
                    ),
                )
            },
        )

        Text("속도 ${String.format(Locale.US, "%.2f", settings.speed)}x", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = settings.speed,
            onValueChange = { onSettingsChange(settings.copy(speed = it)) },
            valueRange = 0.25f..3f,
            steps = 10,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("무한 반복", modifier = Modifier.weight(1f))
            Switch(checked = settings.repeat, onCheckedChange = { onSettingsChange(settings.copy(repeat = it)) })
        }

        LabeledChips(
            label = "렌더 모드",
            values = TesterRenderMode.entries,
            selected = settings.renderMode,
            text = { it.label },
            onSelect = { onSettingsChange(settings.copy(renderMode = it)) },
        )
        LabeledChips(
            label = "스케일",
            values = PreviewScale.entries,
            selected = settings.scale,
            text = { it.label },
            onSelect = { onSettingsChange(settings.copy(scale = it)) },
        )

        Text("미리보기 배경", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("격자", "흰색", "검정", "파랑").forEachIndexed { index, label ->
                FilterChip(
                    selected = settings.backgroundIndex == index,
                    onClick = { onSettingsChange(settings.copy(backgroundIndex = index)) },
                    label = { Text(label) },
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun <T> LabeledChips(
    label: String,
    values: List<T>,
    selected: T,
    text: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Text(label, style = MaterialTheme.typography.labelLarge)
    Row(
        modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        values.forEach { value ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(text(value)) },
            )
        }
    }
}
