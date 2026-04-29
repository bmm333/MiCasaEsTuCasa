package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import org.junit.Rule
import org.junit.Test

class HomeComponentsTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun topAppBar_automation_test() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                Topnavigation()
            }
        }
        composeTestRule.onNodeWithText("MiCasaEsTuCasa", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun searchBar_inputAutomation() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                SearchBar()
            }
        }

        // Trova il TextField tramite il testo del placeholder e inserisce testo
        composeTestRule.onNodeWithText("Dove vai?", substring = true).performTextInput("Milano")
        composeTestRule.onNodeWithText("Milano").assertExists()

        // Sostituisce il testo
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
