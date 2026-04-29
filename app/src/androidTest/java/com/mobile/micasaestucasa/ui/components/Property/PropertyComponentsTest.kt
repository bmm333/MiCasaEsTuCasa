package com.mobile.micasaestucasa.ui.components.Property

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mobile.micasaestucasa.ui.components.property.AmenityItem
import com.mobile.micasaestucasa.ui.components.property.BookingBottomBar
import com.mobile.micasaestucasa.ui.components.property.FeatureChip
import com.mobile.micasaestucasa.ui.components.property.PropertyDetailHeader
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import org.junit.Rule
import org.junit.Test

class PropertyComponentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun propertyHeader_automation_test() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyDetailHeader(
                    title = "Luxury Villa",
                    location = "Amalfi",
                    hostName = "Elena",
                    hostImageRes = android.R.drawable.ic_menu_gallery
                )
            }
        }
        composeTestRule.onNodeWithText("Luxury Villa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Amalfi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Elena", substring = true).assertIsDisplayed()
    }

    @Test
    fun featureChip_checkContent() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                FeatureChip(icon = Icons.Default.Wifi, label = "Beds", value = "2 Bedrooms")
            }
        }
        // ignoreCase aiuta perché il componente potrebbe mettere il testo in UpperCase
        composeTestRule.onNodeWithText("BEDS", ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("2 Bedrooms").assertIsDisplayed()
    }

    @Test
    fun bookingBar_automation_test() {
        val testPrice = "450"
        val testDates = "Jun 12 - 18"

        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                BookingBottomBar(price = testPrice, dates = testDates)
            }
        }

        // Verifica prezzo e date
        composeTestRule.onNodeWithText(testPrice, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(testDates).assertIsDisplayed()

        // Verifica e simula il click sul pulsante prenota
        val bookButton = composeTestRule.onNodeWithText("Book Now", ignoreCase = true)
        bookButton.assertIsDisplayed()
        bookButton.assertHasClickAction()
        bookButton.performClick()
    }

    @Test
    fun amenityItem_display_test() {
        // Supponendo che AmenityItem esista e prenda un testo
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                AmenityItem(icon = Icons.Default.Wifi, label = "Fast WiFi")
            }
        }
        composeTestRule.onNodeWithText("Fast WiFi", ignoreCase = true).assertIsDisplayed()
    }
}
