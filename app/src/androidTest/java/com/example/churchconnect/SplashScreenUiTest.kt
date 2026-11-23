package com.example.churchconnect

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun splashScreen_displaysTitleAndButton() {
        composeTestRule.setContent {
            MaterialTheme {
                SplashScreen(onStart = {})
            }
        }

        // Verify title is displayed
        composeTestRule.onNodeWithText("Church Connect").assertIsDisplayed()

        // Verify subtitle is displayed
        composeTestRule.onNodeWithText("Stay informed. Stay connected.").assertIsDisplayed()

        // Verify continue button is displayed
        composeTestRule.onNodeWithText("Continue to Login").assertIsDisplayed()
    }
}
