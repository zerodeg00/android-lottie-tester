package com.zerodeg.lottietester

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class LottieTesterAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun initialScreenOffersUrlAndLocalFileInputs() {
        composeRule.onNodeWithText("Lottie Tester").assertIsDisplayed()
        composeRule.onNodeWithTag("url-input").assertIsDisplayed()
        composeRule.onNodeWithTag("open-file").assertIsDisplayed()
        composeRule.onNodeWithText("JSON 또는 .lottie 파일을 선택하거나 공개 URL을 입력해 주세요.")
            .assertIsDisplayed()
    }
}
