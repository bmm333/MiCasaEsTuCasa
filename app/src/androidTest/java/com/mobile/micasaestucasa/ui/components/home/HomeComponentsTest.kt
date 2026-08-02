package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.mobile.micasaestucasa.ui.components.nav.MiCasaTopBar
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import org.junit.Rule
import org.junit.Test

class HomeComponentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun topAppBar_automation_test() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                // MiCasaTopBar renders "MiCasa" as the centred brand label
                MiCasaTopBar()
            }
        }
        composeTestRule.onNodeWithText("MiCasa", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun searchBar_inputAutomation() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                SearchBar()
            }
        }

        // The real placeholder text is "Where are you going?"
        composeTestRule.onNodeWithText("Where are you going?", substring = true).performTextInput("Milano")
        composeTestRule.onNodeWithText("Milano").assertExists()

        // Replace the text
        composeTestRule.onNodeWithText("Milano").performTextReplacement("Roma")
        composeTestRule.onNodeWithText("Roma").assertExists()
    }

    @Test
    fun journalSection_buttonInteraction() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                JournalSection()
            }
        }

        composeTestRule.onNodeWithText("THE JOURNAL", ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Join Us", ignoreCase = true).performClick()
    }
}
