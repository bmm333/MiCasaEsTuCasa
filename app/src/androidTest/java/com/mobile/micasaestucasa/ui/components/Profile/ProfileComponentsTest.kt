package com.mobile.micasaestucasa.ui.components.Profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mobile.micasaestucasa.ui.components.profile.HostBanner
import com.mobile.micasaestucasa.ui.components.profile.PersonalInfoCard
import com.mobile.micasaestucasa.ui.components.profile.ProfileHeader
import com.mobile.micasaestucasa.ui.components.profile.SettingsRow
import com.mobile.micasaestucasa.ui.components.profile.WishlistCard
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileComponentsTest {
    @get:Rule val composeTestRule = createComposeRule()

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
