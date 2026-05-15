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
 * Instrumented test per [PropertyImage] e [PropertyThumbnailImage].
 *
 * Copertura:
 *  - url = null  → Coil gestisce il fallback internamente; il nodo è presente.
 *  - url valido  → nodo reso disponibile dalla composizione.
 *  - url vuoto   → branch della stringa vuota delegata a Coil.
 *  - Modifier esterno passato correttamente al nodo.
 */
class PropertyImageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -------------------------------------------------------------------------
    // PropertyImage
    // -------------------------------------------------------------------------

    @Test
    fun propertyImage_withNullUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyImage(
                    imageUrl = null,
                    modifier = Modifier.testTag("property_image_null")
                )
            }
        }
        composeTestRule.onNodeWithTag("property_image_null").assertIsDisplayed()
    }

    @Test
    fun propertyImage_withValidUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyImage(
                    imageUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
                    modifier = Modifier.testTag("property_image_valid")
                )
            }
        }
        composeTestRule.onNodeWithTag("property_image_valid").assertIsDisplayed()
    }

    @Test
    fun propertyImage_withEmptyUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyImage(
                    imageUrl = "",
                    modifier = Modifier.testTag("property_image_empty")
                )
            }
        }
        composeTestRule.onNodeWithTag("property_image_empty").assertIsDisplayed()
    }

    @Test
    fun propertyImage_withNoModifier_isDisplayed() {
        // Verifica che il default Modifier.Companion funzioni senza crash
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyImage(imageUrl = null)
            }
        }
        // Nessuna asserzione di tag — solo verifica che la composizione non lanci eccezioni
    }

    // -------------------------------------------------------------------------
    // PropertyThumbnailImage
    // -------------------------------------------------------------------------

    @Test
    fun propertyThumbnailImage_withNullUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyThumbnailImage(
                    imageUrl = null,
                    modifier = Modifier.testTag("thumbnail_null")
                )
            }
        }
        composeTestRule.onNodeWithTag("thumbnail_null").assertIsDisplayed()
    }

    @Test
    fun propertyThumbnailImage_withValidUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyThumbnailImage(
                    imageUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
                    modifier = Modifier.testTag("thumbnail_valid")
                )
            }
        }
        composeTestRule.onNodeWithTag("thumbnail_valid").assertIsDisplayed()
    }

    @Test
    fun propertyThumbnailImage_withEmptyUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyThumbnailImage(
                    imageUrl = "",
                    modifier = Modifier.testTag("thumbnail_empty")
                )
            }
        }
        composeTestRule.onNodeWithTag("thumbnail_empty").assertIsDisplayed()
    }

    @Test
    fun propertyThumbnailImage_withNoModifier_doesNotCrash() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PropertyThumbnailImage(imageUrl = null)
            }
        }
    }
}
