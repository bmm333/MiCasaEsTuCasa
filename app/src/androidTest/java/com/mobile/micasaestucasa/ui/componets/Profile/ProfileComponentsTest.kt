package com.mobile.micasaestucasa.ui.componets.Profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.mobile.micasaestucasa.ui.components.profile.*
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileComponentsTest {
    @get:Rule 
    val composeTestRule = createComposeRule()

    @Test
    fun profileHeader_showsNameAndBio() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                ProfileHeader(
                    name = "Mario Rossi",
                    memberSince = "2022",
                    bio = "Amo viaggiare",
                    imageRes = android.R.drawable.ic_menu_gallery
                )
            }
        }
        composeTestRule.onNodeWithText("Mario Rossi").assertIsDisplayed()
        composeTestRule.onNodeWithText("2022", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Amo viaggiare", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun personalInfo_showsFields() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PersonalInfoCard(fullName = "Mario Rossi", email = "test@test.com", phone = "123", address = "Via Roma")
            }
        }
        composeTestRule.onNodeWithText("LEGAL NAME", ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Mario Rossi").assertIsDisplayed()
        composeTestRule.onNodeWithText("test@test.com").assertIsDisplayed()
    }

    @Test
    fun wishlist_showsCount_andIsClickable() {
        composeTestRule.setContent { 
            MiCasaEsTuCasaTheme {
                WishlistCard(count = 5) 
            }
        }
        composeTestRule.onNodeWithText("+5").assertIsDisplayed()
        composeTestRule.onNodeWithText("View collections", ignoreCase = true).assertHasClickAction().performClick()
    }

    @Test
    fun hostBanner_automation_test() {
        composeTestRule.setContent { 
            MiCasaEsTuCasaTheme {
                HostBanner() 
            }
        }
        composeTestRule.onNodeWithText("Host Your Home", ignoreCase = true).assertIsDisplayed()
        val button = composeTestRule.onNodeWithText("Get Started", ignoreCase = true)
        button.assertIsDisplayed()
        button.assertHasClickAction()
        button.performClick()
    }

    @Test
    fun settingsRow_triggersAction() {
        var clicked = false
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                SettingsRow(
                    icon = Icons.Default.Settings,
                    label = "Settings Test",
                    onClick = { clicked = true }
                )
            }
        }
        
        composeTestRule.onNodeWithText("Settings Test").performClick()
        assertTrue("L'azione onClick di SettingsRow non è stata attivata!", clicked)
    }
}
