package com.mobile.micasaestucasa.ui.componets.Voyage

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.mobile.micasaestucasa.ui.components.voyage.*
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VoyageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun featuredTripCard_automation_test() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                FeaturedTripCard(
                    title = "Cedar House", 
                    date = "Dec 1", 
                    location = "USA", 
                    host = "Elena", 
                    imageRes = android.R.drawable.ic_menu_gallery,
                )
            }
        }
        composeTestRule.onNodeWithText("Cedar House").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirmed").assertIsDisplayed()
        
        val guideButton = composeTestRule.onNodeWithText("Check-in Guide", ignoreCase = true)
        guideButton.assertHasClickAction()
        guideButton.performClick()
    }

    @Test
    fun nextStep_interaction_test() {
        composeTestRule.setContent { 
            MiCasaEsTuCasaTheme {
                NextStepPlaceholder() 
            }
        }
        composeTestRule.onNodeWithText("Where will you go next?", substring = true).assertIsDisplayed()
        // EXPLORE HOMES non è un vero bottone (non ha onClick), verifichiamo solo la presenza
        composeTestRule.onNodeWithText("EXPLORE HOMES", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun journeysScreen_fullLoad_andScrollTest() {
        composeTestRule.setContent { 
            MiCasaEsTuCasaTheme {
                JourneysScreen() 
            }
        }
        // Verifica caricamento sezioni
        composeTestRule.onNodeWithText("Your Journeys", ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Upcoming", ignoreCase = true).assertIsDisplayed()
        
        // Verifica presenza ricordi passati
        composeTestRule.onNodeWithText("Past Memories", ignoreCase = true).assertIsDisplayed()
    }
}
