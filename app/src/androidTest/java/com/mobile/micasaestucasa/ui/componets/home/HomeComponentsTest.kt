package com.mobile.micasaestucasa.ui.componets.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.mobile.micasaestucasa.ui.components.home.*
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
    fun propertyCard_checkDetails() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyCard(
                    imageRes = android.R.drawable.ic_menu_gallery, 
                    name = "Villa Test", 
                    rating = 4.5, 
                    location = "Roma", 
                    price = 100
                )
            }
        }
        
        composeTestRule.onNodeWithText("Villa Test").assertIsDisplayed()
        composeTestRule.onNodeWithText("100", substring = true).assertIsDisplayed()
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
