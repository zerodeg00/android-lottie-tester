package com.zerodeg.lottietester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PreviewBackground(index: Int, content: @Composable BoxScope.() -> Unit) {
    val modifier = when (index) {
        1 -> Modifier.background(Color.White)
        2 -> Modifier.background(Color.Black)
        3 -> Modifier.background(Color(0xFF28A9E0))
        else -> Modifier.drawBehind {
            val cell = 16.dp.toPx()
            var row = 0
            var y = 0f
            while (y < size.height) {
                var column = 0
                var x = 0f
                while (x < size.width) {
                    drawRect(
                        color = if ((row + column) % 2 == 0) Color(0xFFF4F4F4) else Color(0xFFDCDCDC),
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(cell, cell),
                    )
                    x += cell
                    column++
                }
                y += cell
                row++
            }
        }
    }
    Box(modifier = modifier.fillMaxSize(), content = content)
}
