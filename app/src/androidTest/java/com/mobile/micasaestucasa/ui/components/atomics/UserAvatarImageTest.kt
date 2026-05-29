package com.mobile.micasaestucasa.ui.components.atomics

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented test per [UserAvatarImage] e [UserAvatarLargeImage].
 *
 * Copertura branch:
 *  - imageUrl = null  → Coil mostra il fallback; nodo presente.
 *  - imageUrl valido  → caricamento URL reale; nodo presente.
 *  - imageUrl vuoto   → branch stringa vuota.
 *  - userName viene usato come contentDescription → test indiretto di accessibilità.
 *  - Modifier esterno applicato correttamente.
 */
class UserAvatarImageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -------------------------------------------------------------------------
    // UserAvatarImage (medium — 48×48, cornerPercent=50)
    // -------------------------------------------------------------------------

    @Test
    fun userAvatarImage_withNullUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarImage(
                    imageUrl = null,
                    userName = "Mario Rossi",
                    modifier = Modifier.testTag("avatar_medium_null")
                )
            }
        }
        composeTestRule.onNodeWithTag("avatar_medium_null").assertIsDisplayed()
    }

    @Test
    fun userAvatarImage_withValidUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarImage(
                    imageUrl = "https://i.pravatar.cc/48",
                    userName = "Mario Rossi",
                    modifier = Modifier.testTag("avatar_medium_valid")
                )
            }
        }
        composeTestRule.onNodeWithTag("avatar_medium_valid").assertIsDisplayed()
    }

    @Test
    fun userAvatarImage_withEmptyUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarImage(
                    imageUrl = "",
                    userName = "Mario Rossi",
                    modifier = Modifier.testTag("avatar_medium_empty")
                )
            }
        }
        composeTestRule.onNodeWithTag("avatar_medium_empty").assertIsDisplayed()
    }

    @Test
    fun userAvatarImage_withEmptyUserName_isDisplayed() {
        // Verifica che userName vuoto non causi crash nel contentDescription
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarImage(
                    imageUrl = null,
                    userName = "",
                    modifier = Modifier.testTag("avatar_medium_empty_name")
                )
            }
        }
        composeTestRule.onNodeWithTag("avatar_medium_empty_name").assertIsDisplayed()
    }

    @Test
    fun userAvatarImage_withNoModifier_doesNotCrash() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarImage(imageUrl = null, userName = "Test")
            }
        }
    }

    @Test
    fun userAvatarImage_withSpecialCharactersInUserName_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarImage(
                    imageUrl = null,
                    userName = "Ünîcödé Üser 🏠",
                    modifier = Modifier.testTag("avatar_medium_special")
                )
            }
        }
        composeTestRule.onNodeWithTag("avatar_medium_special").assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // UserAvatarLargeImage (large — 96×96, cornerPercent=50)
    // -------------------------------------------------------------------------

    @Test
    fun userAvatarLargeImage_withNullUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarLargeImage(
                    imageUrl = null,
                    userName = "Laura Lupi",
                    modifier = Modifier.testTag("avatar_large_null")
                )
            }
        }
        composeTestRule.onNodeWithTag("avatar_large_null").assertIsDisplayed()
    }

    @Test
    fun userAvatarLargeImage_withValidUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarLargeImage(
                    imageUrl = "https://i.pravatar.cc/96",
                    userName = "Laura Lupi",
                    modifier = Modifier.testTag("avatar_large_valid")
                )
            }
        }
        composeTestRule.onNodeWithTag("avatar_large_valid").assertIsDisplayed()
    }

    @Test
    fun userAvatarLargeImage_withEmptyUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarLargeImage(
                    imageUrl = "",
                    userName = "Laura Lupi",
                    modifier = Modifier.testTag("avatar_large_empty")
                )
            }
        }
        composeTestRule.onNodeWithTag("avatar_large_empty").assertIsDisplayed()
    }

    @Test
    fun userAvatarLargeImage_withEmptyUserName_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarLargeImage(
                    imageUrl = null,
                    userName = "",
                    modifier = Modifier.testTag("avatar_large_empty_name")
                )
            }
        }
        composeTestRule.onNodeWithTag("avatar_large_empty_name").assertIsDisplayed()
    }

    @Test
    fun userAvatarLargeImage_withNoModifier_doesNotCrash() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                UserAvatarLargeImage(imageUrl = null, userName = "Test")
            }
        }
    }
}
