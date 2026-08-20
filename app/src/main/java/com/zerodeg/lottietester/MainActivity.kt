package com.zerodeg.lottietester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zerodeg.lottietester.ui.LottieTesterApp
import com.zerodeg.lottietester.ui.theme.LottieTesterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LottieTesterTheme {
                LottieTesterApp()
            }
        }
    }
}
