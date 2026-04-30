package com.mobile.micasaestucasa.ui.components.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import org.junit.Rule
import org.junit.Test

class PropertyCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun propertyCard_showsAvailableBadge_whenAvailable() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyCard(
                    name = "Villa Test",
                    rating = 4.5,
                    location = "Roma",
                    price = 100.0,
                    imageUrl = "",
                    isAvailable = true
                )
            }
        }

        composeTestRule.onNodeWithTag("available_badge").assertIsDisplayed()
        composeTestRule.onNodeWithText("AVAILABLE").assertIsDisplayed()
    }

    @Test
    fun propertyCard_hidesAvailableBadge_whenNotAvailable() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyCard(
                    name = "Villa Test",
                    rating = 4.5,
                    location = "Roma",
                    price = 100.0,
                    imageUrl = "",
                    isAvailable = false
                )
            }
        }

        composeTestRule.onNodeWithTag("available_badge").assertDoesNotExist()
    }

    @Test
    fun searchBar_updatesText_andTriggersSearch() {
        var locationText = ""
        var searchClicked = false

        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                SearchBar(
                    location = locationText,
                    onLocationChange = { locationText = it },
                    onSearchClick = { searchClicked = true }
                )
            }
        }

        val input = composeTestRule.onNodeWithTag("search_input_location")
        input.performTextInput("Milano")

        composeTestRule.onNodeWithTag("search_button").performClick()
        assert(searchClicked)
    }

    @Test
    fun bottomNavigationBar_selectsItemOnClick() {
        var selectedIndex = 0
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                BottomNavigationBar(
                    selectedItem = selectedIndex,
                    onItemSelected = { selectedIndex = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("nav_item_Profile").performClick()
        assert(selectedIndex == 3)

        composeTestRule.onNodeWithTag("nav_item_Saved").performClick()
        assert(selectedIndex == 1)
    }

    @Test
    fun footer_displaysCopyrightAndLinks() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                Footer()
            }
        }

        composeTestRule.onNodeWithText("© 2024 MiCasaEsTuCasa. A Warmly Curated Experience.").assertIsDisplayed()
        composeTestRule.onNodeWithText("The Journal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Our Story").assertIsDisplayed()
    }
}
