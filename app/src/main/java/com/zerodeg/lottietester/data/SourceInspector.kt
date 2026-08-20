package com.zerodeg.lottietester.data

import com.zerodeg.lottietester.model.LottieFileFormat
import com.zerodeg.lottietester.model.SourceInspection
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

object SourceInspector {
    private val zipMagic = byteArrayOf(0x50, 0x4b, 0x03, 0x04)

    fun inspect(bytes: ByteArray, mimeType: String?): SourceInspection {
        require(bytes.isNotEmpty()) { "파일이 비어 있습니다." }

        val isZip = bytes.size >= zipMagic.size && zipMagic.indices.all { bytes[it] == zipMagic[it] }
        if (isZip) {
            val jsonEntries = countAnimationJsonEntries(bytes)
            require(jsonEntries > 0) { "ZIP 안에서 Lottie animation JSON을 찾지 못했습니다." }
            return SourceInspection(
                format = LottieFileFormat.DOT_LOTTIE,
                sizeBytes = bytes.size.toLong(),
                mimeType = mimeType,
                animationJsonCount = jsonEntries,
                warnings = if (jsonEntries > 1) {
                    listOf("여러 animation JSON이 있습니다. Lottie 6.6.6은 .lottie manifest의 애니메이션 선택을 지원하지 않아 라이브러리가 선택한 composition을 재생합니다.")
                } else {
                    emptyList()
                },
            )
        }

        val firstToken = bytes.toString(Charsets.UTF_8)
            .asSequence()
            .dropWhile { it.isWhitespace() || it == '\uFEFF' }
            .firstOrNull()
        require(firstToken == '{') { "JSON 또는 .lottie ZIP 형식이 아닙니다." }
        return SourceInspection(
            format = LottieFileFormat.JSON,
            sizeBytes = bytes.size.toLong(),
            mimeType = mimeType,
            animationJsonCount = 1,
            warnings = emptyList(),
        )
    }

    private fun countAnimationJsonEntries(bytes: ByteArray): Int {
        var count = 0
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name.lowercase()
                if (!entry.isDirectory && name.endsWith(".json") && name != "manifest.json") count++
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return count
    }
}
