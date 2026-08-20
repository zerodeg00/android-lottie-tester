package com.zerodeg.lottietester.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zerodeg.lottietester.TesterViewModel
import com.zerodeg.lottietester.model.LoadState
import com.zerodeg.lottietester.model.PlaybackSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LottieTesterApp(viewModel: TesterViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::loadLocal)
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var viewSettings by remember { mutableStateOf(PlaybackSettings.viewDefaults()) }
    var composeSettings by remember { mutableStateOf(PlaybackSettings.composeDefaults()) }
    var viewProgress by remember { mutableFloatStateOf(0f) }
    var composeProgress by remember { mutableFloatStateOf(0f) }
    var viewReport by remember { mutableStateOf(RendererReport()) }
    var composeReport by remember { mutableStateOf(RendererReport()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lottie Tester")
                        Text("Lottie 6.6.6 · Android View / Compose", style = MaterialTheme.typography.labelSmall)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SourceInputCard(
                url = state.urlInput,
                onUrlChange = viewModel::updateUrl,
                onLoadUrl = viewModel::loadUrl,
                onOpenFile = {
                    filePicker.launch(arrayOf("application/json", "application/zip", "application/octet-stream", "*/*"))
                },
                isLoading = state.loadState is LoadState.Loading,
            )

            when (val loadState = state.loadState) {
                LoadState.Idle -> HintCard("JSON 또는 .lottie 파일을 선택하거나 공개 URL을 입력해 주세요.")
                LoadState.Loading -> CircularProgressIndicator(modifier = Modifier.testTag("source-loading"))
                is LoadState.Failed -> ErrorCard(loadState.message, viewModel::retry)
                is LoadState.Ready -> {
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("View · 스플래시 방식") },
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Compose · URL 방식") },
                        )
                    }

                    Card(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                        if (selectedTab == 0) {
                            ViewLottiePane(
                                source = loadState.source,
                                settings = viewSettings,
                                onProgress = { viewProgress = it },
                                onReport = { viewReport = it },
                                modifier = Modifier.fillMaxSize().testTag("view-preview"),
                            )
                        } else {
                            ComposeLottiePane(
                                source = loadState.source,
                                settings = composeSettings,
                                onProgress = { composeProgress = it },
                                onReport = { composeReport = it },
                                modifier = Modifier.fillMaxSize().testTag("compose-preview"),
                            )
                        }
                    }

                    if (selectedTab == 0) {
                        PlaybackControls(
                            settings = viewSettings,
                            progress = viewProgress,
                            onSettingsChange = { viewSettings = it },
                            onResetPreset = { viewSettings = PlaybackSettings.viewDefaults() },
                        )
                        DiagnosticsPanel(loadState.source, "LottieAnimationView", viewReport)
                    } else {
                        PlaybackControls(
                            settings = composeSettings,
                            progress = composeProgress,
                            onSettingsChange = { composeSettings = it },
                            onResetPreset = { composeSettings = PlaybackSettings.composeDefaults() },
                        )
                        DiagnosticsPanel(loadState.source, "Lottie Compose", composeReport)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SourceInputCard(
    url: String,
    onUrlChange: (String) -> Unit,
    onLoadUrl: () -> Unit,
    onOpenFile: () -> Unit,
    isLoading: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Lottie 소스", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth().testTag("url-input"),
                label = { Text("JSON 또는 .lottie URL") },
                placeholder = { Text("https://cdn.example.com/animation.json") },
                singleLine = true,
                enabled = !isLoading,
            )
            Button(
                onClick = onLoadUrl,
                enabled = !isLoading && url.isNotBlank(),
                modifier = Modifier.fillMaxWidth().testTag("load-url"),
            ) { Text("URL 불러오기") }
            OutlinedButton(
                onClick = onOpenFile,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().testTag("open-file"),
            ) { Text("로컬 JSON / .lottie 선택") }
        }
    }
}

@Composable
private fun HintCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(message, modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().testTag("load-error")) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("불러오지 못했습니다", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
            Text(message)
            OutlinedButton(onClick = onRetry) { Text("다시 시도") }
        }
    }
}
