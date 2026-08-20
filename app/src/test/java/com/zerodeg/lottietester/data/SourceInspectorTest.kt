package com.zerodeg.lottietester.data

import com.zerodeg.lottietester.model.LottieFileFormat
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceInspectorTest {
    @Test
    fun `JSON is detected despite whitespace and BOM`() {
        val inspection = SourceInspector.inspect("\uFEFF  \n{\"v\":\"5.7.0\"}".toByteArray(), "application/json")

        assertEquals(LottieFileFormat.JSON, inspection.format)
        assertEquals(1, inspection.animationJsonCount)
        assertTrue(inspection.warnings.isEmpty())
    }

    @Test
    fun `dotLottie ZIP ignores manifest and counts animation JSON files`() {
        val bytes = zipOf(
            "manifest.json" to "{}",
            "animations/first.json" to "{\"v\":\"5.7.0\"}",
            "animations/second.json" to "{\"v\":\"5.7.0\"}",
        )

        val inspection = SourceInspector.inspect(bytes, "application/zip")

        assertEquals(LottieFileFormat.DOT_LOTTIE, inspection.format)
        assertEquals(2, inspection.animationJsonCount)
        assertEquals(1, inspection.warnings.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty input is rejected`() {
        SourceInspector.inspect(byteArrayOf(), null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unsupported input is rejected`() {
        SourceInspector.inspect("not lottie".toByteArray(), "text/plain")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ZIP without an animation JSON is rejected`() {
        SourceInspector.inspect(zipOf("manifest.json" to "{}"), "application/zip")
    }

    private fun zipOf(vararg entries: Pair<String, String>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, body) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(body.toByteArray())
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }
}
