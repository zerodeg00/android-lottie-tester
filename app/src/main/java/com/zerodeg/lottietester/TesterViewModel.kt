package com.zerodeg.lottietester

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zerodeg.lottietester.data.SourceInspector
import com.zerodeg.lottietester.model.LoadState
import com.zerodeg.lottietester.model.LoadedSource
import com.zerodeg.lottietester.model.SourceRef
import com.zerodeg.lottietester.model.TesterUiState
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TesterViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(TesterUiState())
    val uiState: StateFlow<TesterUiState> = _uiState.asStateFlow()

    private val requestIds = AtomicLong(0)
    private var loadJob: Job? = null
    private var lastSource: SourceRef? = null

    fun updateUrl(value: String) {
        _uiState.update { it.copy(urlInput = value) }
    }

    fun loadUrl() {
        val rawUrl = _uiState.value.urlInput.trim()
        val uri = runCatching { URI(rawUrl) }.getOrNull()
        if (uri == null || uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            _uiState.update { it.copy(loadState = LoadState.Failed("공개 HTTP(S) URL을 입력해 주세요.")) }
            return
        }
        startLoad(SourceRef.Remote(rawUrl)) { fetchRemote(rawUrl) }
    }

    fun loadLocal(uri: Uri) {
        val resolver = getApplication<Application>().contentResolver
        val label = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: uri.lastPathSegment ?: "선택한 파일"
        startLoad(SourceRef.Local(uri, label)) {
            val mime = resolver.getType(uri)
            val bytes = requireNotNull(resolver.openInputStream(uri)) { "파일을 열 수 없습니다." }.use { it.readBytes() }
            bytes to mime
        }
    }

    fun retry() {
        when (val source = lastSource) {
            is SourceRef.Local -> loadLocal(source.uri)
            is SourceRef.Remote -> {
                updateUrl(source.url)
                loadUrl()
            }
            null -> if (_uiState.value.urlInput.isNotBlank()) loadUrl()
        }
    }

    private fun startLoad(source: SourceRef, loader: suspend () -> Pair<ByteArray, String?>) {
        loadJob?.cancel()
        lastSource = source
        val requestId = requestIds.incrementAndGet()
        _uiState.update { it.copy(loadState = LoadState.Loading) }
        loadJob = viewModelScope.launch {
            val startedAt = System.nanoTime()
            try {
                val (bytes, mime) = withContext(Dispatchers.IO) { loader() }
                val inspection = withContext(Dispatchers.Default) { SourceInspector.inspect(bytes, mime) }
                val elapsed = (System.nanoTime() - startedAt) / 1_000_000
                if (requestId == requestIds.get()) {
                    _uiState.update {
                        it.copy(loadState = LoadState.Ready(LoadedSource(requestId, source, bytes, inspection, elapsed)))
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (requestId == requestIds.get()) {
                    _uiState.update {
                        it.copy(loadState = LoadState.Failed(error.message ?: error::class.java.simpleName))
                    }
                }
            }
        }
    }

    private fun fetchRemote(url: String): Pair<ByteArray, String?> {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.connect()
            if (connection.responseCode !in 200..299) {
                error("HTTP ${connection.responseCode} ${connection.responseMessage.orEmpty()}".trim())
            }
            connection.inputStream.use { it.readBytes() } to connection.contentType
        } finally {
            connection.disconnect()
        }
    }
}
